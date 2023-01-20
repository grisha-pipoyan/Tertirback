package com.tertir.demo.persistance.qr;

import com.tertir.demo.persistance.user.ApplicationUser;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Min;
import java.util.UUID;

@Entity
@Getter
@Setter
public class QrEntity {

    @Id
    @GeneratedValue
    private UUID Id;

    @Lob
    private String qrDataBase64;

    private Integer counter;

    @Min(0)
    @Column(unique = true)
    private Integer randomCode;

    private boolean printed;

    @OneToOne
    private ApplicationUser user;

}
