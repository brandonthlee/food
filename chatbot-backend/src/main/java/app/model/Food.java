package app.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

import jakarta.persistence.*;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Entity class for representing food items and their attributes
 */
@Entity
@Getter
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Food {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 50, nullable = false)
	private String name;

	@Column(length = 100, nullable = false)
	private String imageUrl;

	@Column(length = 50, nullable = false)
	private String country;

	@Column(length = 50, nullable = false)
	private String flavor;

	@Column(length = 50, nullable = false)
	private String temperature;

	@Column(length = 50, nullable = false)
	private String ingredient;

	@Column(nullable = false)
	private int spicy;

	@Column(length = 50, nullable = false)
	private String oily;

	@ColumnDefault("now()")
	private LocalDateTime createdTime;

	@Builder
	public Food(Long id, String name, String imageUrl, String country, String flavor, String temperature,
			String ingredient, int spicy, String oily, LocalDateTime createdTime) {
		this.id = id;
		this.name = name;
		this.imageUrl = imageUrl;
		this.country = country;
		this.flavor = flavor;
		this.temperature = temperature;
		this.ingredient = ingredient;
		this.spicy = spicy;
		this.oily = oily;
		this.createdTime = createdTime;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getImageUrl() {
		return imageUrl;
	}
}
