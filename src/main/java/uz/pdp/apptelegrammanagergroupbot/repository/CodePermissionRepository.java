package uz.pdp.apptelegrammanagergroupbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.apptelegrammanagergroupbot.entity.CodePermission;

import java.util.List;

@Repository
public interface CodePermissionRepository extends JpaRepository<CodePermission, Long> {
}