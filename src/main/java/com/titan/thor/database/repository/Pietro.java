package com.titan.thor.database.repository;

import com.titan.thor.model.dao.OrderDAO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface Pietro extends JpaRepository<OrderDAO, Long> {}
