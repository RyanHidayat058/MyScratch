package com.myscratch.app.data.network.api

import com.myscratch.app.data.network.dto.*
import retrofit2.Response
import retrofit2.http.*

interface MyScratchApiService {

    // --- APP UPDATE CHECKER ---
    @GET("app/check-update")
    suspend fun checkAppUpdate(): Response<AppUpdateDto>

    // --- AUTH & OTP ---
    @POST("auth/register-request")
    suspend fun registerRequest(
        @Body body: RegisterRequestDto
    ): Response<BaseResponseDto>

    @POST("auth/verify-register-otp")
    suspend fun verifyRegisterOtp(
        @Body body: VerifyRegisterOtpDto
    ): Response<AuthResponseDto>

    @POST("auth/resend-register-otp")
    suspend fun resendRegisterOtp(
        @Body body: ResendRegisterOtpDto
    ): Response<BaseResponseDto>

    @POST("auth/login")
    suspend fun login(
        @Body body: LoginRequestDto
    ): Response<AuthResponseDto>

    @POST("auth/logout")
    suspend fun logout(): Response<BaseResponseDto>

    // --- USER PROFILE & SETTINGS ---
    @GET("user/profile")
    suspend fun getProfile(): Response<ProfileResponseDto>

    @POST("user/change-password-request")
    suspend fun changePasswordRequest(): Response<BaseResponseDto>

    @POST("user/change-password-verify")
    suspend fun changePasswordVerify(
        @Body body: ChangePasswordVerifyDto
    ): Response<BaseResponseDto>

    @POST("user/change-email-request")
    suspend fun changeEmailRequest(
        @Body body: ChangeEmailRequestDto
    ): Response<BaseResponseDto>

    @POST("user/change-email-verify")
    suspend fun changeEmailVerify(
        @Body body: ChangeEmailVerifyDto
    ): Response<ProfileResponseDto>

    @DELETE("user/delete-account")
    suspend fun deleteAccount(): Response<BaseResponseDto>

    // --- FINANCE ---
    @GET("finance/summary")
    suspend fun getFinanceSummary(): Response<FinanceSummaryResponseDto>

    @GET("finance/transactions")
    suspend fun getTransactions(
        @Query("type") type: String? = null,
        @Query("category") category: String? = null,
        @Query("search") search: String? = null
    ): Response<TransactionsResponseDto>

    @POST("finance/transactions")
    suspend fun createTransaction(
        @Body body: TransactionRequestDto
    ): Response<TransactionMutationResponseDto>

    @PUT("finance/transactions/{id}")
    suspend fun updateTransaction(
        @Path("id") id: String,
        @Body body: TransactionRequestDto
    ): Response<TransactionMutationResponseDto>

    @DELETE("finance/transactions/{id}")
    suspend fun deleteTransaction(
        @Path("id") id: String
    ): Response<BaseResponseDto>

    // --- NOTES & FOLDERS ---
    @GET("notes/folders")
    suspend fun getFolders(): Response<FoldersResponseDto>

    @POST("notes/folders")
    suspend fun createFolder(
        @Body body: FolderRequestDto
    ): Response<FolderMutationResponseDto>

    @DELETE("notes/folders/{id}")
    suspend fun deleteFolder(
        @Path("id") id: String
    ): Response<BaseResponseDto>

    @GET("notes")
    suspend fun getNotes(
        @Query("folder_id") folderId: String? = null,
        @Query("search") search: String? = null
    ): Response<NotesResponseDto>

    @POST("notes")
    suspend fun createNote(
        @Body body: NoteRequestDto
    ): Response<NoteMutationResponseDto>

    @PUT("notes/{id}")
    suspend fun updateNote(
        @Path("id") id: String,
        @Body body: NoteRequestDto
    ): Response<NoteMutationResponseDto>

    @DELETE("notes/{id}")
    suspend fun deleteNote(
        @Path("id") id: String
    ): Response<BaseResponseDto>

    @GET("notes/trash")
    suspend fun getTrashedNotes(): Response<NotesResponseDto>

    @POST("notes/{id}/restore")
    suspend fun restoreNote(
        @Path("id") id: String
    ): Response<NoteMutationResponseDto>

    @DELETE("notes/{id}/force-delete")
    suspend fun forceDeleteNote(
        @Path("id") id: String
    ): Response<BaseResponseDto>

    @DELETE("notes/trash/empty")
    suspend fun emptyNotesTrash(): Response<BaseResponseDto>

    // --- SECURE VAULT (BRANKAS RAHASIA) ---
    @GET("vault/items")
    suspend fun getVaultItems(
        @Query("category") category: String? = null,
        @Query("search") search: String? = null
    ): Response<VaultItemsResponseDto>

    @POST("vault/items")
    suspend fun createVaultItem(
        @Body body: VaultItemRequestDto
    ): Response<VaultItemMutationResponseDto>

    @PUT("vault/items/{id}")
    suspend fun updateVaultItem(
        @Path("id") id: String,
        @Body body: VaultItemRequestDto
    ): Response<VaultItemMutationResponseDto>

    @DELETE("vault/items/{id}")
    suspend fun deleteVaultItem(
        @Path("id") id: String
    ): Response<BaseResponseDto>
}
