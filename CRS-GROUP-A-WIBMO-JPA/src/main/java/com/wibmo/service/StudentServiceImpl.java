package com.wibmo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wibmo.entity.Student;
import com.wibmo.repository.StudentRepository;

@Service
public class StudentServiceImpl implements StudentService {

	@Autowired
	private StudentRepository studentRepository;
	
	@Override
	public Student getStudentById(Integer studentId) {
		Optional<Student> studentOptional = studentRepository.findByStudentId(studentId);
		return studentOptional.isPresent()
				? studentOptional.get()
				: null;
	}
	
	@Override
	public List<Student> getAllStudents() {
		return studentRepository.findAll();
	}

	@Override
	public void add(Student student) {
		
		if(null == student) {
			return;
		}
		
		studentRepository.save(student);
	}
	
}
