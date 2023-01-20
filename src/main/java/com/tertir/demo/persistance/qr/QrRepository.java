package com.tertir.demo.persistance.qr;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
public interface QrRepository extends JpaRepository<QrEntity, UUID> {

    List<QrEntity> findAllByQrDataBase64IsNull();

    List<QrEntity> findAllByPrintedFalseAndQrDataBase64IsNotNull();

    Optional<QrEntity> findByRandomCode(Integer randomCode);

    @Query("select r.randomCode from QrEntity r")
    List<Integer> findAllRandomCodeList();

    List<QrEntity> findAllByUserIsNotNull();

}
