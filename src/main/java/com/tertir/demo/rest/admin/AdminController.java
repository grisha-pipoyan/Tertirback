package com.tertir.demo.rest.admin;

import com.tertir.demo.exception.PdfGenerationException;
import com.tertir.demo.exception.QrCodeGenerationException;
import com.tertir.demo.persistance.qr.QrEntity;
import com.tertir.demo.persistance.user.ApplicationUser;
import com.tertir.demo.rest.admin.models.ApplicationUserModel;
import com.tertir.demo.rest.admin.models.QrCodeModel;
import com.tertir.demo.services.QrService;
import com.tertir.demo.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tertir/management")
@CrossOrigin("*")
@Validated
@Slf4j
public class AdminController {

    private final QrService qrService;
    private final UserService userService;

    public AdminController(QrService qrService,
                           UserService userService) {
        this.qrService = qrService;
        this.userService = userService;
    }

    @Operation(summary = "Generate qr codes of n")
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/generate/qr/code/{number}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> generateQrCodes(@PathVariable("number") Integer number) {

        try {
            qrService.generateQrCodes(number);
        } catch (Exception e) {
            String error = String.format("Can not generate QR codes: %s", e.getMessage());
            log.error(error);
            throw new QrCodeGenerationException(error);
        }

        return ResponseEntity.ok(null);
    }


    @Operation(summary = "Get all Qr codes")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/getAllQrCodes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<QrCodeModel>> getAllQrCodes() {
        return ResponseEntity.ok(qrService.getAllQrCodes());
    }


    @Operation(summary = "Get QR code")
    @RequestMapping(value = "/getQrCodeById/{id}", method = RequestMethod.GET,
            produces = MediaType.IMAGE_JPEG_VALUE)
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ByteArrayResource> getQrCodeByID(@PathVariable("id") String id) {

        return ResponseEntity
                .ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(qrService.getQrCodeById(UUID.fromString(id)));

    }

    @Operation(summary = "Created PDF file of Qr images which are not printed yet")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping(value = "/getAllNotPrintedQrCodes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> getAllNotPrintedQrCodes() {

        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            return new ResponseEntity<>(qrService.generatePDFFromAllNonPrintedQrCodes().getByteArray(),
                    httpHeaders, HttpStatus.OK);
        } catch (Exception e) {
            String error = String.format("Error while creating PDF file: %s", e.getMessage());
            log.error(error);
            throw new PdfGenerationException(error);
        }
    }

    @Operation(summary = "Returns all list of users with their sum")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/getAllUsers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ApplicationUserModel>> getAllUsers() {

        List<ApplicationUser> allUsers = userService.getAllUsersEnabledFalse();
        List<ApplicationUserModel> response = new ArrayList<>();
        for (ApplicationUser global :
                allUsers) {
            QrEntity qrEntityByCode = global.getCurrentQrEntity();

            // checking how many people have 5 code
            List<ApplicationUser> fifthRound = userService.findAllUsersWhoScannedCurrentQrEntity(qrEntityByCode);

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
            applicationUserModel.setId(global.getId().toString());
            applicationUserModel.setUsername(global.getUsername());
            applicationUserModel.setIDBankNumber(global.getIDBankNumber());
            applicationUserModel.setOtherBankNumber(global.getOtherBankNumber());
            applicationUserModel.setRandomCode(global.getCurrentQrEntity().getRandomCode());
            applicationUserModel.setSum(global.getSum());
            applicationUserModel.setNumber5(fifthRoundCount);
            applicationUserModel.setNumber4(fourthRoundCount);
            applicationUserModel.setNumber3(thirdRoundCount);
            applicationUserModel.setNumber2(secondRoundCount);
            applicationUserModel.setNumber1(firstRoundCount);

            response.add(applicationUserModel);
        }

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Returns sold papers count")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/soldPapersCount")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Integer> getSoldPapersCount() {
        return ResponseEntity.ok(qrService.getSoldPapersCount());
    }

    @Operation(summary = "Make user purchased")
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/purchaseUser/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> purchase(@PathVariable("id") String id) {
        userService.purchase(UUID.fromString(id));
        return ResponseEntity.ok(null);
    }

    @Operation(summary = "Change password")
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/changePassword")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> changePassword(@RequestParam("oldPassword") String oldPassword,
                                                 @RequestParam("newPassword") String newPassword) {
        userService.changeAdminPassword(oldPassword, newPassword);
        return ResponseEntity.ok(null);
    }

}
