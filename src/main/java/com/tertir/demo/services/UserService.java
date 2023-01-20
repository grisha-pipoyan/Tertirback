package com.tertir.demo.services;

import com.tertir.demo.exception.BadRequestException;
import com.tertir.demo.exception.NotFoundException;
import com.tertir.demo.persistance.qr.QrEntity;
import com.tertir.demo.persistance.user.ApplicationUser;
import com.tertir.demo.persistance.user.ApplicationUserRepository;
import com.tertir.demo.security.user.ApplicationUserRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    private final ApplicationUserRepository applicationUserRepository;
    private final QrService qrService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserService(ApplicationUserRepository applicationUserRepository,
                       QrService qrService, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.applicationUserRepository = applicationUserRepository;
        this.qrService = qrService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return applicationUserRepository.findApplicationUserByUsername(username).orElseThrow(() -> new UsernameNotFoundException
                (String.format("User with name %s not found", username)));
    }

    /**
     * Registers temporary user
     *
     * @param username username
     * @param password password
     */
    public void registerUser(String username, String password) {

        ApplicationUser applicationUser = new ApplicationUser();
        applicationUser.setUsername(username);
        applicationUser.setPassword(bCryptPasswordEncoder.encode(password));
        applicationUser.setRole(ApplicationUserRole.USER);

        applicationUserRepository.save(applicationUser);

    }

    private int generateRandomCode(){
        // Random code generation
        Random random = new Random();
        List<Integer> allQrEntities = qrService.getAllQrEntities();
        int randomCode;
        do{
            randomCode = random.nextInt(100000,3000000);

        }while (allQrEntities.contains(randomCode));

        return randomCode;
    }

    public int saveUserAsStarter(String username,
                                  String idBankNumber,
                                  String otherBankNumber,
                                  QrEntity currentQrEntity) {

        int randomCode = generateRandomCode();
        currentQrEntity.setRandomCode(randomCode);

        ApplicationUser newUser = new ApplicationUser();
        newUser.setUsername(username);
        newUser.setIDBankNumber(idBankNumber);
        newUser.setOtherBankNumber(otherBankNumber);
        newUser.setSum((double) 0);
        newUser.setCurrentQrEntity(currentQrEntity);
        newUser.setRole(ApplicationUserRole.USER);
        newUser.setRegistrationTime(LocalDateTime.now());
        applicationUserRepository.save(newUser);

        currentQrEntity.setUser(newUser);

        qrService.saveQr(currentQrEntity);

        return randomCode;
    }

    public int saveUserAsContinuer(String username,
                                    String idBankNumber,
                                    String otherBankNumber,
                                    QrEntity currentQrEntity,
                                    QrEntity previousQrEntity) {

        int userCounter = 0;
        QrEntity temp = previousQrEntity;
        while (userCounter<5){

            ApplicationUser applicationUser = temp.getUser();
            temp = applicationUser.getPreviousQrEntity();

            userCounter++;
            if(userCounter==5){
                Double sum = applicationUser.getSum();
                sum+=500;
                applicationUser.setSum(sum);
                applicationUserRepository.save(applicationUser);
            }
            if(temp==null){
                break;
            }
        }

        Integer counter = previousQrEntity.getCounter();
        counter++;
        if (counter > 4) {
            throw new NotFoundException("Qr կոդն արդեն սպառված է։");
        }
        previousQrEntity.setCounter(counter);

        int randomCode = generateRandomCode();
        currentQrEntity.setRandomCode(randomCode);

        ApplicationUser newUser = new ApplicationUser();
        newUser.setUsername(username);
        newUser.setIDBankNumber(idBankNumber);
        newUser.setOtherBankNumber(otherBankNumber);
        newUser.setSum((double) 0);
        newUser.setCurrentQrEntity(currentQrEntity);
        newUser.setPreviousQrEntity(previousQrEntity);
        newUser.setRole(ApplicationUserRole.USER);
        newUser.setRegistrationTime(LocalDateTime.now());

        applicationUserRepository.save(newUser);

        currentQrEntity.setUser(newUser);
        qrService.saveQr(currentQrEntity);

        return randomCode;
    }


    public List<ApplicationUser> getAllUsersEnabledFalse(){
        return applicationUserRepository.findAllByEnabledFalse();
    }

    /**
     * Purchase user
     * @param id user id
     */
    public void purchase(UUID id) {
        ApplicationUser applicationUser = applicationUserRepository.findById(id).orElseThrow(() -> new NotFoundException(
                String.format("User with id %s not found", id)));
        applicationUser.setSum((double) 0);

        applicationUserRepository.save(applicationUser);
    }

    public List<ApplicationUser> findAllUsersWhoScannedCurrentQrEntity(QrEntity qrEntity) {
        return applicationUserRepository.findAllByPreviousQrEntityEquals(qrEntity);
    }

    public void changeAdminPassword(String oldPassword, String newPassword){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String principal = (String) authentication.getPrincipal();
        ApplicationUser user = (ApplicationUser) loadUserByUsername(principal);

        if(!bCryptPasswordEncoder.matches(oldPassword, user.getPassword())){
            throw new BadRequestException("Հին գաղտնաբառը սխալ է։");
        }

        String newEncoded = bCryptPasswordEncoder.encode(newPassword);
        user.setPassword(newEncoded);

        applicationUserRepository.save(user);

    }
}
