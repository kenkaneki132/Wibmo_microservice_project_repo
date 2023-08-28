package com.wibmo.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wibmo.dao.CourseDAOImpl;
import com.wibmo.dto.CourseProfessorDTO;
import com.wibmo.entity.Course;
import com.wibmo.entity.Professor;
import com.wibmo.enums.CourseType;
import com.wibmo.exception.CannotDropCourseAssignedToProfessorException;
import com.wibmo.exception.CourseNotExistsInCatalogException;
import com.wibmo.exception.UserNotFoundException;

@Service
public class CourseServiceImpl implements CourseService {

	@Autowired
	private UserServiceImpl userOperation;
	
	@Autowired
	private ProfessorServiceImpl professorOperation;
	
	@Autowired
	private CourseDAOImpl courseDAO;
	
	// TODO: This implementation can be moved to Join query in Database
	@Override
	public List<CourseProfessorDTO> getCourseDetailsBySemester(Integer semester) {
		
		List<Course> courses = courseDAO.findAllBySemester(semester);
		Map<Integer, Professor> professorIdToProfessorMap = professorOperation
				.getProfessorIdToProfessorMap(
						courses
							.stream()
							.map(course -> course.getProfessorId())
							.filter(professorId -> professorId != null)
							.collect(Collectors.toSet()));
		
		return courses
				.stream()
				.map(course -> new CourseProfessorDTO(
						course, 
						null != course.getProfessorId() 
							? professorIdToProfessorMap.get(course.getProfessorId())
							: null))
				.collect(Collectors.toList());
	}

	@Override
	public Map<Integer, Course> getCourseIdToCourseMap(Set<Integer> courseIds) {
		return courseDAO
				.findAllByCourseIdIn(courseIds)
				.stream()
				.collect(Collectors.toMap(
						Course::getCourseId,
						Function.identity()));
	}

	@Override
	public List<Course> getCoursesAssignedToProfessor(Integer professorId) 
			throws UserNotFoundException {
		
		if(!userOperation.isUserExistsById(professorId)) {
			throw new UserNotFoundException(professorId);
		}
		
		return courseDAO
				.findAllByProfessorId(professorId);
	}
	
	@Override
	public CourseType getCourseTypeByCourseId(Integer courseId) 
			throws CourseNotExistsInCatalogException {
		
		if(!isCourseExistsInCatalog(courseId)) {
			throw new CourseNotExistsInCatalogException(courseId);
		}
		
		return courseDAO
				.findCourseTypeByCourseId(courseId);
	}

	@Override
	public Boolean isCourseExistsInCatalog(Integer courseId) {
		return courseDAO
				.existsByCourseId(courseId);
	}

	@Override
	public List<Course> getAllCourses() {
		return courseDAO.findAll();
	}

	@Override
	public Boolean add(Course course) {
		
		// TODO: Move to Validator
		if(null == course || null != course.getCourseId()) {
			return Boolean.FALSE;
		}
		
		return courseDAO.save(course); 
	}

	@Override
	public Boolean removeCourseById(Integer courseId) 
			throws 
				CourseNotExistsInCatalogException, 
				CannotDropCourseAssignedToProfessorException {
		
		if(!isCourseExistsInCatalog(courseId)) {
			throw new CourseNotExistsInCatalogException(courseId);
		}
		
		Integer professorId = courseDAO.findProfessorIdByCourseId(courseId);
		
		if(null != professorId) {
			throw new CannotDropCourseAssignedToProfessorException(courseId, professorId);
		}
		
		return courseDAO.deleteCourseById(courseId);
	}

	@Override
	public void assignCourseToProfessor(Integer courseId, Integer professorId) 
			throws 
				CourseNotExistsInCatalogException, 
				UserNotFoundException {
		
		if(!isCourseExistsInCatalog(courseId)) {
			throw new CourseNotExistsInCatalogException(courseId);
		}
		
		if(!userOperation.isUserExistsById(professorId)) {
			throw new UserNotFoundException(professorId);
		}
		
		courseDAO.setProfessorIdAsWhereCourseIdIs(professorId, courseId);
	}

	@Override
	public Boolean isProfessorAssignedForCourse(Integer professorId, Integer courseId)
		throws 
			UserNotFoundException, 
			CourseNotExistsInCatalogException {

		if(null == professorId || null == courseId) {
			return false;
		}
		
		if(!userOperation.isUserExistsById(professorId)) {
			throw new UserNotFoundException(professorId);
		}
		
		if(!isCourseExistsInCatalog(courseId)) {
			throw new CourseNotExistsInCatalogException(courseId);
		}
		
		return null != professorId
				&& professorId.equals(
						courseDAO.findProfessorIdByCourseId(courseId));
	}

}
