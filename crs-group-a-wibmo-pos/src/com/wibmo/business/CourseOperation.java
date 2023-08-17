package com.wibmo.business;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.wibmo.bean.Course;

public interface CourseOperation {

	/**
	 * 
	 * @param currentSemester
	 */
	public void viewCourseDetailsBySemester(Integer currentSemester);
	
	/**
	 * 
	 * @param courseIds
	 * @return
	 */
	public Map<Integer, Course> getCourseIdToCourseMap(Set<Integer> courseIds);
	
	/**
	 * 
	 * @param professor
	 * @return
	 */
	public List<Course> getCoursesAssignedToProfessor(Integer professorId);

	/**
	 * To add
	 */
	public void viewAllCourses();
	public boolean addCourse(Course course);
	public boolean removeCourseById(int courseId);
	public void assignCourseToProfessor(int courseId, int professorId);

}
