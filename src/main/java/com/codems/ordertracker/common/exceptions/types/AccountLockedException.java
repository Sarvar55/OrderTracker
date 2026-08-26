package com.codems.ordertracker.common.exceptions.types;

public class AccountLockedException extends BaseException {

    public AccountLockedException() {
        super(CommonErrorType.ACCOUNT_LOCKED);
    }

    public static AccountLockedException locked() {
        return new AccountLockedException();
    }
}
