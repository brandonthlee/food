package app.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Entity class for the user's food preferences
 */
@Entity
@Getter
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserPreference {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Food food;

    @ColumnDefault("now()")
    private LocalDateTime createdTime;

    @Builder
    public UserPreference(Long id, User user, Food food, LocalDateTime createdTime) {
        this.id = id;
        this.user = user;
        this.food = food;
        this.createdTime = createdTime;
    }

	public Food getFood() {
		return food;
	}
}
