package com.pranshulgg.recordmaster.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pranshulgg.recordmaster.R
import com.pranshulgg.recordmaster.ui.components.Symbol
import com.pranshulgg.recordmaster.utils.bottomPadding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import com.pranshulgg.recordmaster.utils.topPadding
import dev.jeziellago.compose.markdowntext.MarkdownText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(snackbarHostState: SnackbarHostState, navController: NavController) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Box(
                modifier = Modifier
                    .height(150.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(bottomEnd = 28.dp, bottomStart = 28.dp),
                ) {

                    Row(
                        modifier = Modifier.padding(
                            top = topPadding() + 12.dp,
                            end = 16.dp,
                            start = 16.dp,
                            bottom = 16.dp
                        ),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        Column(
                            verticalArrangement = Arrangement.spacedBy(
                                space = 5.dp,
                                alignment = Alignment.CenterVertically
                            )
                        ) {
                            Text(
                                "RecordMaster",
                                fontSize = 24.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            CheckForUpdateBtn(
                                currentVersion = "v1.1.0",
                                githubRepo = "PranshulGG/RecordMaster",
                                snackbarHostState = snackbarHostState
                            )
                        }
                        Image(
                            painter = painterResource(id = R.drawable.main_app_icon),
                            contentDescription = "App icon",
                            modifier = Modifier.size(80.dp)
                        )
                    }


                }
            }

        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(
                top = innerPadding.calculateTopPadding(),
                start = innerPadding.calculateStartPadding(layoutDirection = LayoutDirection.Ltr),
                end = innerPadding.calculateEndPadding(layoutDirection = LayoutDirection.Ltr)
            )
        ) {
            Spacer(Modifier.height(12.dp))
            ListItem(
                modifier = Modifier.height(68.dp),
                headlineContent = { Text("Licenses") },
                supportingContent = { Text("GNU GPL-3.0") },
                leadingContent = { IconAvatarLeading(R.drawable.license_24px) }
            )
            ListItem(
                headlineContent = { Text("Email") },
                supportingContent = { Text("pranshul.devmain@gmail.com") },
                leadingContent = { IconAvatarLeading(R.drawable.mail_24px) },
                modifier = Modifier
                    .clickable(
                        onClick = {
                            uriHandler.openUri("mailto:pranshul.devmain@gmail.com")
                        }
                    )
                    .height(68.dp)
            )
            ListItem(
                headlineContent = { Text("Source code") },
                supportingContent = { Text("On Github") },
                leadingContent = { IconAvatarLeading(R.drawable.code_24px) },
                modifier = Modifier
                    .clickable(
                        onClick = {
                            uriHandler.openUri("https://github.com/PranshulGG/RecordMaster")
                        }
                    )
                    .height(68.dp)
            )
            ListItem(
                headlineContent = { Text("Create an issue") },
                supportingContent = { Text("On Github") },
                leadingContent = { IconAvatarLeading(R.drawable.bug_report_24px) },
                modifier = Modifier
                    .clickable(
                        onClick = {
                            uriHandler.openUri("https://github.com/PranshulGG/RecordMaster/issues/")
                        }
                    )
                    .height(68.dp)
            )

            Spacer(Modifier.weight(1f))
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),

                    ) {
                    Column(
                        modifier = Modifier.padding(top = 12.dp)
                    ) {
                        ListItem(
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            ),
                            headlineContent = { Text("Third party licenses") },
                            leadingContent = { IconAvatarLeading(R.drawable.copyright_24px) },
                            modifier = Modifier
                                .clickable(
                                    onClick = {
//                                        navController.navigate("OpenAboutLibScreen")
                                    }
                                )
                                .height(60.dp)
                        )
                        ListItem(
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            ),
                            headlineContent = { Text("Terms & conditions") },
                            leadingContent = { IconAvatarLeading(R.drawable.contract_24px) },
                            modifier = Modifier
                                .clickable(
                                    onClick = {
                                        navController.navigate("OpenTermsConditionScreen")
                                    }
                                )
                                .height(60.dp)
                        )
                        ListItem(
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            ),
                            headlineContent = { Text("Privacy policy") },
                            leadingContent = { IconAvatarLeading(R.drawable.privacy_tip_24px) },
                            modifier = Modifier
                                .clickable(
                                    onClick = {
                                        navController.navigate("OpenPrivacyPolicyScreen")
                                    }
                                )
                                .height(60.dp)
                        )
                        Spacer(Modifier.height(bottomPadding()))
                    }
                }

            }
        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CheckForUpdateBtn(
    currentVersion: String,
    githubRepo: String = "PranshulGG/RecordMaster",
    snackbarHostState: SnackbarHostState? = null
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var isChecking by remember { mutableStateOf(false) }
    val size = SplitButtonDefaults.SmallContainerHeight

    val showMessage: suspend (String) -> Unit = { msg ->
        if (snackbarHostState != null) {
            snackbarHostState.showSnackbar(msg)
        } else {
            withContext(Dispatchers.Main) {
                Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun openReleasesPage() {
        val url = "https://github.com/$githubRepo/releases"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        ctx.startActivity(intent)
    }

    suspend fun fetchLatestStableTag(repo: String): String? = withContext(Dispatchers.IO) {
        val apiUrl = "https://api.github.com/repos/$repo/releases"
        val url = URL(apiUrl)
        var conn: HttpURLConnection? = null
        try {
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10_000
                readTimeout = 10_000
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "Compose-App")
            }

            if (conn.responseCode != HttpURLConnection.HTTP_OK) {
                throw Exception("HTTP ${conn.responseCode}")
            }

            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val arr = JSONArray(body)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val prerelease = obj.optBoolean("prerelease", false)
                if (!prerelease) {
                    return@withContext obj.optString("tag_name", null)
                }
            }
            return@withContext null
        } finally {
            conn?.disconnect()
        }
    }

    OutlinedButton(

        modifier = Modifier.heightIn(size),
//        contentPadding = SplitButtonDefaults.leadingButtonContentPaddingFor(size),
        shapes = ButtonDefaults.shapes(),
        onClick = {
            if (isChecking) return@OutlinedButton
            scope.launch {
                isChecking = true
                try {
                    val latest = fetchLatestStableTag(githubRepo)
                    delay(300)

                    if (latest != null && latest != currentVersion) {
                        showMessage("New version available: $latest")
                        delay(800)
                        openReleasesPage()
                    } else {
                        showMessage("You are using the latest version!")
                    }
                } catch (t: Throwable) {
                    showMessage("Error checking for updates")
                    t.printStackTrace()
                } finally {
                    isChecking = false
                }
            }
        }
    ) {
        Symbol(R.drawable.refresh_24px, size = 17.dp, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text(
            if (isChecking) "Checking..." else currentVersion,
            fontSize = 15.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }

}

@Composable
fun IconAvatarLeading(icon: Int) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
        Symbol(icon, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

// TERMS PAGE

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsPage(navController: NavController) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()


    val markdownData = """
These Terms & Conditions apply to the **RecordMaster** app (the "Application") for mobile devices.  
This app was created by **Pranshul** as an open-source, hobby project. By using the Application, you agree to the following:  

## Use of the Application  
- The Application is provided **as-is**, free of charge, and without any guarantees of reliability, availability, or accuracy.  
- You may use, modify, and distribute the Application in accordance with its open-source license.  
- You may **not** misrepresent the origin of the Application or use its name/trademarks without permission.  

## Data & Privacy  
- The Application does **not collect, store, or share** any personal information.  
- The Application may request permission to send **notifications**, which are handled entirely on your device.  
- For more details, please see the Privacy Policy.  

## Liability  
- The Service Provider (Pranshul) is **not liable** for any direct or indirect damages, losses, or issues that may arise from using the Application.  
- This includes (but is not limited to) inaccurate information, device issues, or mobile data charges.  
- You are responsible for ensuring your device is compatible and has sufficient internet and battery to use the Application.  

## Updates & Availability  
- The Application may be updated from time to time.  
- There is no guarantee that the Application will always remain available, functional, or supported on all operating system versions.  
- The Service Provider may discontinue the Application at any time without prior notice.  

## Changes to These Terms  
These Terms & Conditions may be updated in the future. Updates will be posted in the project repository or within the Application. Continued use of the Application means you accept any revised terms.  

## Contact  
If you have any questions about these Terms & Conditions, please contact:  
📧 **pranshul.devmain@gmail.com**
"""

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Terms & Conditions") },
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Symbol(
                            R.drawable.arrow_back_24px,
                            desc = "Back",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(top = innerPadding.calculateTopPadding(), start = 16.dp, end = 16.dp)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            item {
                MarkdownText(
                    markdown = markdownData,
                    linkColor = MaterialTheme.colorScheme.tertiary,
                    onLinkClicked = { url ->
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    }
                )
            }
            item {
                Spacer(Modifier.height(bottomPadding() + 12.dp))
            }
        }
    }

}

// PRIVACY PAGE

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PolicyPage(navController: NavController) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()


    val markdownData = """
RecordMaster is an open-source application.  

We respect your privacy. This application:  

- Does **not collect, store, or share** any personal information.  
- Does **not track** your IP address, usage data, or any identifiers.  
- Does **not send data** to us or to third parties.  

## Notifications  
The app may request permission to send you notifications.  
- Granting notification permission is **Required on Android 12+**.  
- Notifications are generated and handled **locally on your device**.  
- No notification data is collected, stored, or shared.  

## Children  
This application does not knowingly collect any personal information from anyone, including children under 13.  

## Changes  
If this Privacy Policy changes, we will update it here. Continued use of the app after changes means you accept the revised policy.  

## Contact  
If you have any questions about privacy while using RecordMaster, please contact us at:  
📧 **pranshul.devmain@gmail.com**  

"""

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Privacy policy") },
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Symbol(
                            R.drawable.arrow_back_24px,
                            desc = "Back",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(top = innerPadding.calculateTopPadding(), start = 16.dp, end = 16.dp)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            item {
                MarkdownText(
                    markdown = markdownData,
                    linkColor = MaterialTheme.colorScheme.tertiary,
                    onLinkClicked = { url ->
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    }
                )
            }
            item {
                Spacer(Modifier.height(bottomPadding() + 12.dp))
            }
        }
    }

}