package com.tertir.demo.persistance.user;

import com.tertir.demo.persistance.qr.QrEntity;
import com.tertir.demo.security.user.ApplicationUserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
public interface ApplicationUserRepository extends JpaRepository<ApplicationUser, UUID> {


    Optional<ApplicationUser> findApplicationUserByUsername(String username);

    Optional<ApplicationUser> findApplicationUserByIDBankNumber(String idBankNumber);

    Optional<ApplicationUser> findApplicationUserByOtherBankNumber(String bankNumber);

    List<ApplicationUser> findAllByPreviousQrEntityEquals(QrEntity qrEntity);

    List<ApplicationUser> findAllByEnabledFalse();

    List<ApplicationUser> findApplicationUserByRoleEquals(ApplicationUserRole role);

}
