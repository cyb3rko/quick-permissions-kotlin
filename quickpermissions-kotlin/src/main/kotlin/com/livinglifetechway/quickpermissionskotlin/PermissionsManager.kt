/*
 * Copyright (c) 2023-2025 Cyb3rKo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.livinglifetechway.quickpermissionskotlin

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.livinglifetechway.quickpermissionskotlin.util.PermissionCheckerFragment
import com.livinglifetechway.quickpermissionskotlin.util.PermissionsUtil
import com.livinglifetechway.quickpermissionskotlin.util.QuickPermissionsOptions
import com.livinglifetechway.quickpermissionskotlin.util.QuickPermissionsRequest

private const val TAG = "runWithPermissions"

/**
 * Injects code to ask for permissions before executing any code that requires permissions
 * defined in the annotation
 */
fun Context?.runWithPermissions(
    vararg permissions: String,
    options: QuickPermissionsOptions = QuickPermissionsOptions(),
    callback: () -> Unit
) = runWithPermissionsHandler(this, permissions, callback, options)

/**
 * Injects code to ask for permissions before executing any code that requires permissions
 * defined in the annotation
 */
@Suppress("unused")
fun Fragment?.runWithPermissions(
    vararg permissions: String,
    options: QuickPermissionsOptions = QuickPermissionsOptions(),
    callback: () -> Unit
): Any? = runWithPermissionsHandler(this, permissions, callback, options)

private fun runWithPermissionsHandler(
    target: Any?,
    permissions: Array<out String>,
    callback: () -> Unit,
    options: QuickPermissionsOptions
): Nothing? {
    // get the permissions defined in annotation
    Log.d(TAG, "runWithPermissions: permissions to check: ${permissions.joinToString()}")

    // get target
    if (target is AppCompatActivity || target is Fragment) {
        Log.d(TAG, "runWithPermissions: context found")

        val context = when (target) {
            is Context -> target
            is Fragment -> target.context
            else -> null
        }

        // check if we have the permissions
        if (PermissionsUtil.hasSelfPermission(context, arrayOf(*permissions))) {
            Log.d(
                TAG,
                "runWithPermissions: already has required permissions. Proceed with the execution."
            )
            callback()
        } else {
            // we don't have required permissions
            // begin the permission request flow

            Log.d(TAG, "runWithPermissions: doesn't have required permissions")

            // execute preRationaleAction
            options.preRationaleAction?.let { it() }

            // check if we have permission checker fragment already attached

            // support for AppCompatActivity and Activity
            var permissionCheckerFragment = when (context) {
                // for app compat activity
                is AppCompatActivity -> {
                    context.supportFragmentManager.findFragmentByTag(
                        PermissionCheckerFragment::class.java.canonicalName
                    ) as PermissionCheckerFragment?
                }
                // for fragment
                is FragmentActivity -> {
                    context.supportFragmentManager.findFragmentByTag(
                        PermissionCheckerFragment::class.java.canonicalName
                    ) as PermissionCheckerFragment?
                }
                // else return null
                else -> null
            }

            // check if permission check fragment is added or not
            // if not, add that fragment
            if (permissionCheckerFragment == null) {
                Log.d(TAG, "runWithPermissions: adding headless fragment for asking permissions")
                permissionCheckerFragment = PermissionCheckerFragment.newInstance()
                when (context) {
                    is AppCompatActivity -> {
                        context.supportFragmentManager.beginTransaction().apply {
                            add(
                                permissionCheckerFragment,
                                PermissionCheckerFragment::class.java.canonicalName
                            )
                            commit()
                        }
                        // make sure fragment is added before we do any context based operations
                        context.supportFragmentManager.executePendingTransactions()
                    }
                    is FragmentActivity -> {
                        // this does not work at the moment
                        context.supportFragmentManager.beginTransaction().apply {
                            add(
                                permissionCheckerFragment,
                                PermissionCheckerFragment::class.java.canonicalName
                            )
                            commit()
                        }
                        // make sure fragment is added before we do any context based operations
                        context.supportFragmentManager.executePendingTransactions()
                    }
                }
            }

            // set callback to permission checker fragment
            permissionCheckerFragment.setListener(
                object : PermissionCheckerFragment.QuickPermissionsCallback {
                    override fun onPermissionsGranted(
                        quickPermissionsRequest: QuickPermissionsRequest?
                    ) {
                        Log.d(TAG, "runWithPermissions: got permissions")
                        try {
                            callback()
                        } catch (throwable: Throwable) {
                            throwable.printStackTrace()
                        }
                    }

                    override fun onPermissionsDenied(
                        quickPermissionsRequest: QuickPermissionsRequest?
                    ) {
                        quickPermissionsRequest?.permissionsDeniedMethod?.invoke(
                            quickPermissionsRequest
                        )
                    }

                    override fun shouldShowRequestPermissionsRationale(
                        quickPermissionsRequest: QuickPermissionsRequest?
                    ) {
                        quickPermissionsRequest?.rationaleMethod?.invoke(quickPermissionsRequest)
                    }

                    override fun onPermissionsPermanentlyDenied(
                        quickPermissionsRequest: QuickPermissionsRequest?
                    ) {
                        quickPermissionsRequest?.permanentDeniedMethod?.invoke(
                            quickPermissionsRequest
                        )
                    }
                }
            )

            // create permission request instance
            val permissionRequest = QuickPermissionsRequest(
                permissionCheckerFragment,
                arrayOf(*permissions)
            )
            permissionRequest.handleRationale = options.handleRationale
            permissionRequest.handlePermanentlyDenied = options.handlePermanentlyDenied
            permissionRequest.rationaleMessage = options.rationaleMessage.ifBlank {
                "These permissions are required to perform this feature. Please allow us to use " +
                    "this feature. "
            }
            permissionRequest.permanentlyDeniedMessage = options.permanentlyDeniedMessage.ifBlank {
                "Some permissions are permanently denied which are required to perform this " +
                    "operation. Please open app settings to grant these permissions."
            }
            permissionRequest.rationaleMethod = options.rationaleMethod
            permissionRequest.permanentDeniedMethod = options.permanentDeniedMethod
            permissionRequest.permissionsDeniedMethod = options.permissionsDeniedMethod

            // begin the flow by requesting permissions
            permissionCheckerFragment.setRequestPermissionsRequest(permissionRequest)

            // start requesting permissions for the first time
            permissionCheckerFragment.requestPermissionsFromUser()
        }
    } else {
        // context is null
        // cannot handle the permission checking from the any class other than Activity/Fragment
        // crash the app RIGHT NOW!
        throw IllegalStateException(
            "Found " + target!!::class.java.canonicalName + " : No support from any classes " +
                "other than AppCompatActivity/Fragment"
        )
    }
    return null
}
