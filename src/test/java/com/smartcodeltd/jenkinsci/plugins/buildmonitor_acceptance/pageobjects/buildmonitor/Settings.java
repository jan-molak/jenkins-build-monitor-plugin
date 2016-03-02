package com.smartcodeltd.jenkinsci.plugins.buildmonitor_acceptance.pageobjects.buildmonitor;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class Settings {
    private final WebElement root;

    public Settings(WebElement root) {
        this.root = root;
    }

    public String fontSize() {
        return root.findElement(By.xpath("//ul/li[2]/slider/span[contains(@class, 'bubble ng-binding')]")).getText();
    }

    public String numberOfColumns() {
        return root.findElement(By.xpath("//ul/li[3]/slider/span[contains(@class, 'bubble ng-binding')]")).getText();
    }
}