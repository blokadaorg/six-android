/*
 * This file is part of Blokada.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * Copyright © 2021 Blocka AB. All rights reserved.
 *
 * @author Karol Gusak (karol@blocka.net)
 */

package ui.web

import android.content.ComponentName
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import binding.StageBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.blokada.R
import service.AlertDialogService
import ui.BottomSheetFragment
import ui.SettingsViewModel
import ui.app
import ui.utils.openInBrowser
import utils.Links

class WebFragment : BottomSheetFragment() {

    private lateinit var settingsVM: SettingsViewModel
    private val stage by lazy { StageBinding }

    companion object {
        fun newInstance() = WebFragment()
    }

    private val webService = WebService
    private val alertService = AlertDialogService

    private var tabsServiceBound = false
    private var waitingToComeBack = false
    private lateinit var currentUrl: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.let {
            settingsVM = ViewModelProvider(it.app()).get(SettingsViewModel::class.java)
        }

        val url = arguments?.getString("url") ?: throw Exception("No url provided")

        currentUrl = url

        setHasOptionsMenu(true)

        val root = inflater.inflate(R.layout.fragment_web, container, false)

        val loading: View = root.findViewById(R.id.web_loading)
        val loadingText: TextView = root.findViewById(R.id.web_loading_text)

        val openBrowser: View = root.findViewById(R.id.web_openinbrowser)
        openBrowser.setOnClickListener {
            launchInBrowser(url)
        }

        if ((settingsVM.getUseChromeTabs() || Links.isAvoidWebView(url))
            && (tabsServiceBound || bindChromeTabs())) {
            launchInCustomTabs(url)
        } else {
            // Only instantiate WebView if we're not using custom tabs
            lifecycleScope.launchWhenCreated {
                val container: ViewGroup = root.findViewById(R.id.container)
                val webView = webService.getWebView(object : WebService.Interaction {

                    override fun onOpenInBrowser(url: String) = launchInBrowser(url)

                    override fun onDownload(url: String) {
                        alertService.showAlert(
                            message = getString(R.string.alert_download_link_body),
                            title = getString(R.string.universal_action_open_in_browser),
                            positiveAction = getString(R.string.universal_action_open_in_browser) to {
                                launchInBrowser(currentUrl)
                            }
                        )
                    }

                    override fun onLoaded(url: String) {
                        loading.visibility = View.GONE
                        performUrlSpecificActions(url)
                    }

                    override fun onLoadedWithError(urlo: String, error: String) {
                        if (urlo == url) {
                            context?.let { ctx ->
                                loadingText.text = ctx.getString(R.string.error_fetching_data)
                            }
                        } else {
                            loading.visibility = View.GONE
                        }
                    }

                    override fun onWentBackToTheBeginning() {
                        try {
                            stage.goBack()
                        } catch (ex: Exception) {
                            // It may happen if user navigated away from the app quickly. Ignore
                        }
                    }

                })
                container.addView(webView)

                webService.resetHistory()
                webView.loadUrl(url)
            }
        }

        return root
    }

    private fun launchInCustomTabs(url: String) {
        val builder = CustomTabsIntent.Builder()
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(requireContext(), Uri.parse(url))
        requireContext().unbindService(connection)
        waitToComeBack()
//        performActionsAfterExternalViewScenario(url)
    }

    private fun launchInBrowser(url: String) {
        try {
            openInBrowser(url)
            waitToComeBack()
//            performActionsAfterExternalViewScenario(url)
        } catch (e: Exception) {}
    }

    private fun performUrlSpecificActions(url: String) {
        currentUrl = url
        // TODO: replace this?
//        activationVM.maybeRefreshAccountAfterUrlVisited(url)
    }

//    private fun performActionsAfterExternalViewScenario(url: String) {
//        lifecycleScope.launch {
//            delay(2000) // To not flag it before we get the onResume() call
//
//            // This is to refresh account after we are back from custom tabs or external browser
//            if (Links.isSubscriptionLink(url)) activationVM.setStartedPurchaseFlow()
//        }
//    }

    private fun waitToComeBack() {
        lifecycleScope.launch {
            delay(2000) // To not flag it before we get the onResume() call
            waitingToComeBack = true
        }
    }

    private fun finishWhenCameBack() {
        if (waitingToComeBack) {
            lifecycleScope.launch {
                delay(1000) // So that user sees we went back
                stage.goBack()
                waitingToComeBack = false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // TODO: also this
//        activationVM.maybeRefreshAccountAfterOnResume()
        finishWhenCameBack()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.web_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.web_openinbrowser -> {
                launchInBrowser(currentUrl)
                true
            }
            else -> false
        }
    }

    var connection: CustomTabsServiceConnection = object : CustomTabsServiceConnection() {
        override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
            tabsServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            tabsServiceBound = false
        }
    }

    fun bindChromeTabs() = CustomTabsClient.bindCustomTabsService(requireContext(),
        CUSTOM_TAB_PACKAGE_NAME, connection)
}

private const val CUSTOM_TAB_PACKAGE_NAME = "com.android.chrome"
