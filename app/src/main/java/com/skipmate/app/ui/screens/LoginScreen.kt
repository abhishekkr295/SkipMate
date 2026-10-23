package com.skipmate.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skipmate.app.R
import com.skipmate.app.data.auth.AuthRepository
import com.skipmate.app.data.auth.AuthResult
import com.skipmate.app.data.user.UserProfileRepository
import com.skipmate.app.ui.theme.Primary
import com.skipmate.app.ui.theme.StatusError
import com.skipmate.app.ui.theme.StatusSuccess
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    authRepository: AuthRepository,
    userProfileRepository: UserProfileRepository,
    onLoginSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotPasswordEmail by remember { mutableStateOf("") }
    var resetEmailSentMessage by remember { mutableStateOf<String?>(null) }

    fun performLogin() {
        if (isLoading) return
        errorMessage = null

        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            errorMessage = "Please enter a valid email address."
            return
        }
        if (password.isBlank()) {
            errorMessage = "Please enter your password."
            return
        }

        isLoading = true
        focusManager.clearFocus()

        coroutineScope.launch {
            when (val result = authRepository.login(trimmedEmail, password)) {
                is AuthResult.Success -> {
                    // Sync / retrieve user profile from Firestore
                    userProfileRepository.createProfileIfMissing(
                        uid = result.data.uid,
                        name = result.data.displayName.ifBlank { "User" },
                        email = result.data.email
                    )
                    isLoading = false
                    onLoginSuccess()
                }
                is AuthResult.Error -> {
                    isLoading = false
                    errorMessage = result.message
                }
                is AuthResult.Loading -> {
                    isLoading = true
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Logo & Brand Heading
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_skipmate_logo),
                    contentDescription = "SkipMate Logo",
                    modifier = Modifier.size(72.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Welcome to SkipMate",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Sign in to access your sync settings and subscription",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Error Banner
            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = StatusError.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = StatusError,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = StatusError
                        )
                    }
                }
            }

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorMessage = null
                },
                label = { Text("Email address") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = Primary)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null
                },
                label = { Text("Password") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Primary)
                },
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { performLogin() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                enabled = !isLoading
            )

            // Forgot Password Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = {
                        forgotPasswordEmail = email.trim()
                        showForgotPasswordDialog = true
                    },
                    enabled = !isLoading
                ) {
                    Text(
                        text = "Forgot Password?",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = StatusSuccess
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sign In Button
            Button(
                onClick = { performLogin() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sign Up Link Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Don't have an account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                TextButton(
                    onClick = onNavigateToSignUp,
                    enabled = !isLoading
                ) {
                    Text(
                        text = "Sign Up",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = StatusSuccess
                    )
                }
            }
        }
    }

    // Forgot Password Dialog
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = {
                Text(
                    text = "Reset Password",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter your registered email address and we'll send you instructions to reset your password.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = forgotPasswordEmail,
                        onValueChange = { forgotPasswordEmail = it },
                        label = { Text("Email address") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    if (resetEmailSentMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = resetEmailSentMessage ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = StatusSuccess
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (forgotPasswordEmail.isNotBlank()) {
                            coroutineScope.launch {
                                authRepository.sendPasswordResetEmail(forgotPasswordEmail.trim())
                                resetEmailSentMessage = "Password reset email sent! Check your inbox."
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Send Reset Link")
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
