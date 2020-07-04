/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.feature.containers

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runBlockingTest
import mozilla.components.browser.state.action.ContainerAction
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.state.ContainerState
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.support.test.ext.joinBlocking
import mozilla.components.support.test.mock
import mozilla.components.support.test.robolectric.testContext
import mozilla.components.support.test.whenever
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class ContainerMiddlewareTest {

    @Test
    fun `container storage stores the provided container on add container action`() = runBlockingTest {
        val storage: ContainerStorage = mock()
        val middleware = ContainerMiddleware(testContext, coroutineContext, storage)
        val store = BrowserStore(
            initialState = BrowserState(),
            middleware = listOf(middleware)
        )
        val container = ContainerState(
            contextId = "contextId",
            name = "Personal",
            color = ContainerState.Color.GREEN,
            icon = ContainerState.Icon.CART
        )

        store.dispatch(ContainerAction.AddContainerAction(container)).joinBlocking()

        verify(storage).addContainer(
            container.contextId,
            container.name,
            container.color,
            container.icon
        )
    }

    @Test
    fun `fetch the containers from the container storage and load into browser state on initialize container state action`() = runBlockingTest {
        val storage: ContainerStorage = mock()
        val middleware = ContainerMiddleware(testContext, coroutineContext, storage)
        val store = spy(BrowserStore(
            initialState = BrowserState(),
            middleware = listOf(middleware)
        ))
        // val containers = emptyList<Container>()

        whenever(storage.getContainers()).thenReturn(flow { listOf(
            ContainerState(
                contextId = "contextId",
                name = "Personal",
                color = ContainerState.Color.GREEN,
                icon = ContainerState.Icon.CART
            )
        ) })

        store.dispatch(ContainerAction.InitializeContainerState).joinBlocking()

        verify(storage).getContainers()
        // assertTrue(store.state.containers.containsKey("contextId"))
    }

    @Test
    fun `container storage removes the provided container on remove container action`() = runBlockingTest {
        val storage: ContainerStorage = mock()
        val middleware = ContainerMiddleware(testContext, coroutineContext, storage)
        val container = ContainerState(
            contextId = "contextId",
            name = "Personal",
            color = ContainerState.Color.GREEN,
            icon = ContainerState.Icon.CART
        )
        val store = BrowserStore(
            initialState = BrowserState(
                containers = mapOf(
                    container.contextId to container
                )
            ),
            middleware = listOf(middleware)
        )

        store.dispatch(ContainerAction.RemoveContainerAction(container.contextId)).joinBlocking()

        verify(storage).removeContainer(container)
    }
}
