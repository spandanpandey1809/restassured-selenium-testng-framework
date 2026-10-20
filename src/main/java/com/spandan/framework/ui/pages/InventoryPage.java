package com.spandan.framework.ui.pages;

import com.spandan.framework.ui.BasePage;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

public class InventoryPage extends BasePage {

    private final By pageTitle = By.cssSelector("[data-test='title']");
    private final By itemNames = By.cssSelector("[data-test='inventory-item-name']");
    private final By itemPrices = By.cssSelector("[data-test='inventory-item-price']");
    private final By cartBadge = By.cssSelector("[data-test='shopping-cart-badge']");
    private final By cartLink = By.cssSelector("[data-test='shopping-cart-link']");
    private final By sortDropdown = By.cssSelector("[data-test='product-sort-container']");

    public InventoryPage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        return getText(pageTitle).equals("Products");
    }

    public List<String> getItemNames() {
        waitForVisible(itemNames);
        return findAll(itemNames).stream().map(WebElement::getText).toList();
    }

    public List<Double> getItemPrices() {
        waitForVisible(itemPrices);
        return findAll(itemPrices).stream()
                .map(e -> Double.parseDouble(e.getText().replace("$", "")))
                .toList();
    }

    /** Converts a product name like "Sauce Labs Backpack" to its add-to-cart button id. */
    @Step("Add '{productName}' to cart")
    public InventoryPage addToCart(String productName) {
        String slug = productName.toLowerCase().replace(" ", "-");
        click(By.cssSelector("[data-test='add-to-cart-" + slug + "']"));
        return this;
    }

    @Step("Sort products by '{visibleText}'")
    public InventoryPage sortBy(String visibleText) {
        new Select(waitForVisible(sortDropdown)).selectByVisibleText(visibleText);
        return this;
    }

    public int getCartCount() {
        return isDisplayed(cartBadge) ? Integer.parseInt(getText(cartBadge)) : 0;
    }

    @Step("Open cart")
    public CartPage openCart() {
        click(cartLink);
        return new CartPage(driver);
    }
}
