package uz.pdp.apptelegrammanagergroupbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.apptelegrammanagergroupbot.entity.PaymentUnBot;

@Repository
public interface PaymentUnBotRepository extends JpaRepository<PaymentUnBot, Long> {
}