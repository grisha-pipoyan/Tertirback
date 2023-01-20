package com.tertir.demo;

import com.tertir.demo.persistance.user.ApplicationUser;
import com.tertir.demo.persistance.user.ApplicationUserRepository;
import com.tertir.demo.security.user.ApplicationUserRole;
import com.tertir.demo.security.user.PasswordEncoder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CommandLineAppStartupRunner implements CommandLineRunner {

    private final ApplicationUserRepository applicationUserRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public CommandLineAppStartupRunner(ApplicationUserRepository applicationUserRepository,
                                       BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.applicationUserRepository = applicationUserRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    public void run(String... args) {

        List<ApplicationUser> admins =
                applicationUserRepository.findApplicationUserByRoleEquals(ApplicationUserRole.ADMIN);

        if(admins.size()==0){
            ApplicationUser applicationUser = new ApplicationUser();
            applicationUser.setUsername("vache");
            applicationUser.setPassword(bCryptPasswordEncoder.encode("password"));
            applicationUser.setRole(ApplicationUserRole.ADMIN);
            applicationUser.setEnabled(true);
            applicationUserRepository.save(applicationUser);
        }

    }
}
