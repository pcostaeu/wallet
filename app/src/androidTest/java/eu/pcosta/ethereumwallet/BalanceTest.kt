package eu.pcosta.ethereumwallet

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertContains
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertNotDisplayed
import com.adevinta.android.barista.rule.flaky.AllowFlaky
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Thread.sleep

@RunWith(AndroidJUnit4::class)
@LargeTest
class BalanceTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    @AllowFlaky(attempts = 3)
    fun testHomeScreen() {
        // Check address value
        assertDisplayed(R.id.address_value)
        assertContains(R.id.address_value, BuildConfig.ETH_ACCOUNT)

        // Check initial state
        assertDisplayed(R.id.progress_indicator)
        assertNotDisplayed(R.id.balance_amount)
        assertNotDisplayed(R.id.balance_value)
        assertNotDisplayed(R.id.balance_last_update)
        assertNotDisplayed(R.id.balance_error_update)

        // Wait for balance reply
        sleep(1000)

        // Check that views are now visible
        assertNotDisplayed(R.id.progress_indicator)
        assertNotDisplayed(R.id.balance_error_update)
        assertDisplayed(R.id.balance_amount)
        assertDisplayed(R.id.balance_value)
        assertDisplayed(R.id.balance_last_update)
    }

}