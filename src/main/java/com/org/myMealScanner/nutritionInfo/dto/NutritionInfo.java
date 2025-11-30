package com.org.myMealScanner.nutritionInfo.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "food_nutrition")
@Getter
@NoArgsConstructor
public class NutritionInfo {
    @Id
    @Column(name = "food_name", nullable = false, length = 50)
    private String foodName;

    @Column(name = "energy_kcal")
    private Integer energyKcal;

    @Column(name = "carbohydrate_g")
    private Float carbohydrateG;

    @Column(name = "protein_g")
    private Float proteinG;

    @Column(name = "fat_g")
    private Float fatG;
}
