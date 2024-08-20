package uz.pdp.apptelegrammanagergroupbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.apptelegrammanagergroupbot.entity.UserPermission;

@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermission, Long> {
}