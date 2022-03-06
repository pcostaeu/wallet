package eu.pcosta.ethereumwallet

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.adevinta.android.barista.assertion.BaristaListAssertions.assertDisplayedAtPosition
import com.adevinta.android.barista.assertion.BaristaRecyclerViewAssertions.assertRecyclerViewItemCount
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertNotDisplayed
import com.adevinta.android.barista.interaction.BaristaClickInteractions.clickOn
import com.adevinta.android.barista.interaction.BaristaEditTextInteractions.writeTo
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Thread.sleep


@RunWith(AndroidJUnit4::class)
@LargeTest
class SearchTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testSearchScreen() {
        assertDisplayed(R.id.tokens_search_btn)
        clickOn(R.id.tokens_search_btn)

        // Check empty state visible
        assertDisplayed(R.id.empty_state)
        assertRecyclerViewItemCount(R.id.recycler_view, 0)

        // Search for USDT
        writeTo(R.id.search_view, "USDT")
        sleep(1000)

        // We should have one row, empty state gone
        assertNotDisplayed(R.id.empty_state)
        assertRecyclerViewItemCount(R.id.recycler_view, 1)
        assertDisplayedAtPosition(R.id.recycler_view, 0, R.id.token_name, "Tether USD (USDT):")

        // Invalid search
        writeTo(R.id.search_view, "USDTT")
        sleep(1000)

        // We should have empty state visible
        assertDisplayed(R.id.empty_state)
        assertRecyclerViewItemCount(R.id.recycler_view, 0)

        // Search for USDT
        writeTo(R.id.search_view, "USDT")
        sleep(1000)

        // We should have one row, empty state gone
        assertNotDisplayed(R.id.empty_state)
        assertRecyclerViewItemCount(R.id.recycler_view, 1)
        assertDisplayedAtPosition(R.id.recycler_view, 0, R.id.token_name, "Tether USD (USDT):")

        // Clear text
        writeTo(R.id.search_view, "")
        sleep(1000)

        // We should have empty state visible
        assertDisplayed(R.id.empty_state)
        assertRecyclerViewItemCount(R.id.recycler_view, 0)
    }
}