package ru.netology.nmedia.repository

import java.io.File

interface AuthRepository {
    suspend fun singIn(login: String, pass: String?)
    suspend fun singUp(login: String, pass: String?, name: String?, media: File?)
}