package com.example.planad

import com.example.planad.screens.auth.AuthUtils
import com.google.rpc.context.AttributeContext.Auth
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.junit.Assert

class EmailValidTest {

    @Test
    fun testEmailValidation() {
        val validEmail = "test@example.com"
        assertTrue(AuthUtils.isEmailValid(validEmail))

        // Невалидный email (отсутствует символ @)
        val invalidEmail = "test.example.com"
        assertFalse(AuthUtils.isEmailValid(invalidEmail))

        // Невалидный email (отсутствует домен)
        val invalidEmail2 = "test@"
        assertFalse(AuthUtils.isEmailValid(invalidEmail2))

        // Невалидный email (пустая строка)
        val invalidEmail3 = ""
        assertFalse(AuthUtils.isEmailValid(invalidEmail3))
    }
}


