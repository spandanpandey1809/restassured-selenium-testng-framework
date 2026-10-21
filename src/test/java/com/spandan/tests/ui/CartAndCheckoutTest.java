package com.spandan.tests.ui;

import com.spandan.framework.config.ConfigManager;
import com.spandan.framework.ui.pages.CartPage;
import com.spandan.framework.ui.pages.CheckoutPage;
import com.spandan.framework.ui.pages.InventoryPage;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Epic("Sauce Demo Web")
@Feature("Cart and checkout")
public class CartAndCheckoutTest extends BaseUiTest {

    private static final String BACKPACK = "Sauce Labs Backpack";
    private static final String BIKE_LIGHT = "Sauce Labs Bike Light";

    private InventoryPage inventory;

    // TestNG runs the parent's @BeforeMethod (startBrowser) before this one.
    @BeforeMethod(alwaysRun = true)
    public void login() {
        inventory = loginPage().loginAs(ConfigManager.get("ui.username"), ConfigManager.get("ui.password"));
    }

    @Test(groups = {"ui", "smoke"})
    @Description("Adding two products updates the cart badge and the cart lists both")
    public void addProductsToCart() {
        inventory.addToCart(BACKPACK).addToCart(BIKE_LIGHT);
        assertEquals(inventory.getCartCount(), 2, "Cart badge count");

        CartPage cart = inventory.openCart();
        assertEquals(cart.getItemNames(), List.of(BACKPACK, BIKE_LIGHT), "Items in cart");
    }

    @Test(groups = {"ui", "regression"})
    @Description("Sorting by price low to high orders products correctly")
    public void sortByPriceLowToHigh() {
        List<Double> prices = inventory.sortBy("Price (low to high)").getItemPrices();

        List<Double> expected = new ArrayList<>(prices);
        expected.sort(Comparator.naturalOrder());
        assertEquals(prices, expected, "Prices should be in ascending order");
    }

    @Test(groups = {"ui", "smoke"})
    @Description("User can complete an order end to end")
    public void completeCheckout() {
        String confirmation = inventory.addToCart(BACKPACK)
                .openCart()
                .checkout()
                .enterDetails("Spandan", "Pandey", "560001")
                .finish()
                .getConfirmationMessage();

        assertEquals(confirmation, "Thank you for your order!");
    }

    @Test(groups = {"ui", "regression"})
    @Description("Checkout requires a first name")
    public void checkoutWithoutFirstNameShowsError() {
        CheckoutPage checkout = inventory.addToCart(BACKPACK).openCart().checkout()
                .enterDetails("", "Pandey", "560001");

        assertTrue(checkout.getErrorMessage().contains("First Name is required"));
    }
}
