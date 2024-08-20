package uz.pdp.apptelegrammanagergroupbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.apptelegrammanagergroupbot.entity.CodePermission;
import uz.pdp.apptelegrammanagergroupbot.entity.DontUsedCodePermission;

import java.util.List;

@Repository
public interface DontUsedCodePermissionRepository extends JpaRepository<DontUsedCodePermission, Long> {
    List<DontUsedCodePermission> findAllByCode(String text);
}