package app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import app.model.UserPreference;

@Repository
public interface UserPreferenceRepository {
	@Modifying
    @Query("DELETE FROM Favor f WHERE f.user.id = :userId")
    void deleteAllByUserId(Long userId);

    @Query("SELECT f FROM Favor f JOIN FETCH f.food fo WHERE f.user.id=:userId")
    List<UserPreference> findByUserId(Long userId);

	void saveAll(List<UserPreference> preference);
}
