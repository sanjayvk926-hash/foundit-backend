package com.foundit.service;

import com.foundit.model.Match;
import com.foundit.model.Notification;
import com.foundit.model.User;
import com.foundit.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public void createMatchNotification(User user, Match match) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMatch(match);
        notification.setType(Notification.NotificationType.MATCH_FOUND);
        notification.setMessage("Good news! We found a potential match for your item.");
        notificationRepository.save(notification);
    }

    public void createAdminNotification(User user, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(Notification.NotificationType.ADMIN_UPDATE);
        notification.setMessage(message);
        notificationRepository.save(notification);
    }

    public List<Notification> getUnreadNotifications(User user) {
        // This would ideally use a custom query in the repository
        return notificationRepository.findAll().stream()
                .filter(n -> n.getUser().getId().equals(user.getId()) && !n.getIsRead())
                .toList();
    }
}
