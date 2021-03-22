package com.jrmf.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class ShareSignRequest implements Serializable {

    private User user;
    private AgreementTemplate agreementTemplate;
    private UsersAgreement usersAgreement;
    private int signSubmitType;
}
