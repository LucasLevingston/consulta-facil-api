package com.consultafacil.application.port.in.notification;

public interface CountUnreadNotificationsUseCase {

    long execute(String userId);
}
