package com.mutuelle.mobille.service.notifications.config;

import com.mutuelle.mobille.dto.notifications.NotificationRequestDto;
import com.mutuelle.mobille.enums.NotificationChannel;

public interface NotificationSender {

    NotificationChannel getChannel();

    void send(NotificationRequestDto request);
}