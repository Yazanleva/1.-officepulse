package com.officepulse.app

import org.junit.Assert.*
import org.junit.Test

class AuthValidatorTest {
    @Test fun emailMustHaveOneAtSignAndADomain() {
        listOf("", " ", "student", "a@@b.com", "a b@c.com", "a@b").forEach {
            assertNotNull("Should reject '$it'", AuthValidator.validateEmail(it))
        }
        assertNull(AuthValidator.validateEmail(" student+lab@university.edu "))
    }

    @Test fun passwordMinimumIsSixCharacters() {
        assertNotNull(AuthValidator.validatePassword(""))
        assertNotNull(AuthValidator.validatePassword("12345"))
        assertNull(AuthValidator.validatePassword("123456"))
    }

    @Test fun confirmationMustBePresentAndMatchExactly() {
        assertNotNull(AuthValidator.validatePasswordConfirmation("abcdef", ""))
        assertNotNull(AuthValidator.validatePasswordConfirmation("abcdef", "abcdeF"))
        assertNull(AuthValidator.validatePasswordConfirmation("abcdef", "abcdef"))
    }
}
