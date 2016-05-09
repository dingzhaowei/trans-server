package com.ding.trans.server.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ding.trans.server.model.Account;

public class AccountManager {

    private static Logger log = LogManager.getLogger(AccountManager.class);

    private static AccountManager instance = new AccountManager();

    private AccountManager() {
    }

    public static AccountManager instance() {
        return instance;
    }

    public String getPassword(String userName) {
        Account account = TransDao.instance().getAccount(userName);
        return account == null ? null : account.getPassword();
    }

    public void changePassword(String userName, String newPass) {
        log.info("User {} changed its password", userName);
    }

}
