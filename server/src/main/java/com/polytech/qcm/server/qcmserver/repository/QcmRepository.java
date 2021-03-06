package com.polytech.qcm.server.qcmserver.repository;

import com.polytech.qcm.server.qcmserver.data.QCM;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * MySQL repository
 * MySQL requests are handled directly by hibernates (jpa)
 */
@Repository
public interface QcmRepository extends JpaRepository<QCM, Integer> {

  void deleteById(int id);

  List<QCM> findAllByAuthor_Username(String username);

}
