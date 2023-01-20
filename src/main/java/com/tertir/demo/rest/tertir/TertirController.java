package com.tertir.demo.rest.tertir;

import com.tertir.demo.exception.BadRequestException;
import com.tertir.demo.exception.ConflictException;
import com.tertir.demo.exception.NotFoundException;
import com.tertir.demo.persistance.qr.QrEntity;
import com.tertir.demo.persistance.user.ApplicationUser;
import com.tertir.demo.rest.admin.models.ApplicationUserModel;
import com.tertir.demo.services.QrService;
import com.tertir.demo.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tertir/public")
@CrossOrigin("*")
@Validated
public class TertirController {

    private final UserService userService;
    private final QrService qrService;

    public TertirController(UserService userService,
                            QrService qrService) {
        this.userService = userService;
        this.qrService = qrService;
    }

    @Operation(summary = "Registering user as starter")
    @PostMapping("/register/user/starter")
    public int registerUserAsStarter(@RequestParam("username") String username,
                                     @RequestParam(value = "idBankNumber", required = false) String idBankNumber,
                                     @RequestParam(value = "otherBankNumber", required = false) String otherBankNumber,
                                     @RequestParam("currentQrCodeID") String currentQrCodeID) {

        QrEntity currentQrEntity = qrService.getQrEntityById(UUID.fromString(currentQrCodeID));

        if (currentQrEntity.getUser() != null) {
            throw new ConflictException("Qr կոդն արդեն սկանավորված է։");
        }

        try {
            return userService.saveUserAsStarter(username, idBankNumber, otherBankNumber, currentQrEntity);
        } catch (Exception e) {
            throw new BadRequestException("Տեղի է ունեցել սխալ");
        }

    }

    @Operation(summary = "Registering user as a continuer")
    @PostMapping("/register/user/continuer")
    public int registerUserAsContinuer(@RequestParam("username") String username,
                                       @RequestParam(value = "idBankNumber", required = false) String idBankNumber,
                                       @RequestParam(value = "otherBankNumber", required = false) String otherBankNumber,
                                       @RequestParam("currentQrCodeID") String currentQrCodeID,
                                       @RequestParam("previousQrCodeID") String previousQrCodeID) {

        QrEntity currentQrEntity = qrService.getQrEntityById(UUID.fromString(currentQrCodeID));

        if (currentQrEntity.getUser() != null) {
            throw new ConflictException("Qr կոդն արդեն սկանավորված է։");
        }

        QrEntity previousQrEntity = qrService.getQrEntityByRandomCode(Integer.valueOf(previousQrCodeID));

        try {
            return userService.saveUserAsContinuer(username, idBankNumber,
                    otherBankNumber, currentQrEntity, previousQrEntity);
        } catch (Exception e) {
            if (e instanceof NotFoundException) {
                throw e;
            } else {
                throw new BadRequestException("Տեղի է ունեցել սխալ");
            }
        }
    }

    @Operation(summary = "Check if qr is used already")
    @GetMapping("/qr/check/{id}")
    public ResponseEntity<Boolean> findIfQrAlreadyScanned(@PathVariable("id") String id) {
        QrEntity qrEntityById = qrService.getQrEntityById(UUID.fromString(id));
        return ResponseEntity.ok(qrEntityById.getUser()==null);
    }

    @Operation(summary = "Show code statistics")
    @GetMapping("/getStatistics/{randomCode}")
    public ResponseEntity<ApplicationUserModel> getCodeStatistics(@PathVariable("randomCode") Integer randomCode) {

        QrEntity qrEntityByCode = qrService.getQrEntityByRandomCode(randomCode);
        ApplicationUser user = qrEntityByCode.getUser();

        // checking how many people have 5 code
        List<ApplicationUser> fifthRound =
                userService.findAllUsersWhoScannedCurrentQrEntity(qrEntityByCode);

        int fifthRoundCount = fifthRound.size();
        int fourthRoundCount = 0;
        int thirdRoundCount = 0;
        int secondRoundCount = 0;
        int firstRoundCount = 0;

        for (ApplicationUser user5 :
                fifthRound) {

            // checking how many people have 4 code
            List<ApplicationUser> fourthRound =
                    userService.findAllUsersWhoScannedCurrentQrEntity(user5.getCurrentQrEntity());
            fourthRoundCount += fourthRound.size();

            for (ApplicationUser user4 :
                    fourthRound) {

                // checking how many people have 3 code
                List<ApplicationUser> thirdRound =
                        userService.findAllUsersWhoScannedCurrentQrEntity(user4.getCurrentQrEntity());
                thirdRoundCount += thirdRound.size();

                for (ApplicationUser user3 :
                        thirdRound) {

                    // checking how many people have 2 code
                    List<ApplicationUser> secondRound =
                            userService.findAllUsersWhoScannedCurrentQrEntity(user3.getCurrentQrEntity());
                    secondRoundCount += secondRound.size();

                    for (ApplicationUser user2 :
                            secondRound) {

                        // checking how many people have 2 code
                        List<ApplicationUser> firstRound =
                                userService.findAllUsersWhoScannedCurrentQrEntity(user2.getCurrentQrEntity());
                        firstRoundCount += firstRound.size();

                    }
                }
            }
        }

        ApplicationUserModel applicationUserModel = new ApplicationUserModel();
        applicationUserModel.setId(user.getId().toString());
        applicationUserModel.setUsername(user.getUsername());
        applicationUserModel.setIDBankNumber(user.getIDBankNumber());
        applicationUserModel.setOtherBankNumber(user.getOtherBankNumber());

        applicationUserModel.setNumber5(fifthRoundCount);
        applicationUserModel.setNumber4(fourthRoundCount);
        applicationUserModel.setNumber3(thirdRoundCount);
        applicationUserModel.setNumber2(secondRoundCount);
        applicationUserModel.setNumber1(firstRoundCount);

        return ResponseEntity.ok(applicationUserModel);

    }

}
