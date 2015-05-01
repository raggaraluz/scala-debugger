package com.senkbeil.debugger.events

import com.sun.jdi.VirtualMachine
import com.sun.jdi.event.{VMStartEvent, Event, EventSet, EventQueue}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{OneInstancePerTest, Matchers, FunSpec}

import scala.util.Try
import EventType._

class EventManagerSpec extends FunSpec with Matchers with MockFactory
  with OneInstancePerTest
{
  private val mockVirtualMachine = mock[VirtualMachine]
  private val mockLoopingTaskRunner = mock[LoopingTaskRunner]
  private val eventManager =
    new EventManager(mockVirtualMachine, mockLoopingTaskRunner)

  describe("EventManager") {
    describe("#start") {
      it("should throw an exception if already started") {
        (mockLoopingTaskRunner.addTask _).expects(*).once()
        eventManager.start()

        intercept[IllegalArgumentException] {
          eventManager.start()
        }
      }

      it("should add a task to process events") {
        (mockLoopingTaskRunner.addTask _).expects(*).once()

        eventManager.start()
      }

      describe("task added to runner") {
        it("should pull the next event off of the virtual machine's event queue") {
          var addTaskFunction: Option[() => Any] = None

          // Capture the task passed to the runner
          (mockLoopingTaskRunner.addTask _).expects(*).onCall { arg: Any =>
            addTaskFunction = Some(arg.asInstanceOf[() => Any])
            "": LoopingTaskRunner#TaskId
          }

          eventManager.start()

          // Fail the test if we couldn't retrieve the argument
          if (addTaskFunction.isEmpty) fail("Unable to capture task!")

          val mockEventQueue = mock[EventQueue]
          (mockEventQueue.remove: () => EventSet).expects().once()

          (mockVirtualMachine.eventQueue _).expects().returning(mockEventQueue)

          // Invoke the task function, suppressing any errors since not fully
          // mocking the function's components
          Try(addTaskFunction.get.apply())
        }

        it("should iterate through each event and execute the associated handlers") {
          // Use specific type such that EventType.eventToEventType works
          val eventType = VMStartEventType
          val mockEvent = mock[VMStartEvent]

          var addTaskFunction: Option[() => Any] = None
          var isInvoked = false

          eventManager.addEventHandler(eventType, (event) => {
            isInvoked = true
          })

          // Capture the task passed to the runner
          (mockLoopingTaskRunner.addTask _).expects(*).onCall { arg: Any =>
            addTaskFunction = Some(arg.asInstanceOf[() => Any])
            "": LoopingTaskRunner#TaskId
          }

          eventManager.start()

          // Fail the test if we couldn't retrieve the argument
          if (addTaskFunction.isEmpty) fail("Unable to capture task!")

          // Mock chain of calls in task to return mock event from set
          (mockVirtualMachine.eventQueue _).expects().returning({
            val mockEventQueue = mock[EventQueue]
            (mockEventQueue.remove: () => EventSet).expects().returning({
              val mockEventSet = mock[EventSet]
              (mockEventSet.iterator _).expects().returning({
                val mockIterator = mock[java.util.Iterator[Event]]
                inSequence {
                  (mockIterator.hasNext _).expects().returning(true).once()
                  (mockIterator.hasNext _).expects().returning(false).once()
                }
                (mockIterator.next _).expects().returning(mockEvent)

                mockIterator
              })
              (mockEventSet.resume _).expects().once()
              mockEventSet
            })
            mockEventQueue
          })

          // Invoke the task function
          addTaskFunction.get.apply()

          // Should have invoked handler for event
          isInvoked should be (true)
        }

        it("should not invoke a handler not associated with the event") {
          val stubEventType = stub[EventType]
          val mockEvent = mock[Event]
          var addTaskFunction: Option[() => Any] = None
          var isInvoked = false

          trait OtherEvent extends Event

          eventManager.addEventHandler(stubEventType, (event) => {
            isInvoked = true
          })

          // Capture the task passed to the runner
          (mockLoopingTaskRunner.addTask _).expects(*).onCall { arg: Any =>
            addTaskFunction = Some(arg.asInstanceOf[() => Any])
            "": LoopingTaskRunner#TaskId
          }

          eventManager.start()

          // Fail the test if we couldn't retrieve the argument
          if (addTaskFunction.isEmpty) fail("Unable to capture task!")

          // Mock chain of calls in task to return mock event from set
          (mockVirtualMachine.eventQueue _).expects().returning({
            val mockEventQueue = mock[EventQueue]
            (mockEventQueue.remove: () => EventSet).expects().returning({
              val mockEventSet = mock[EventSet]
              (mockEventSet.iterator _).expects().returning({
                val mockIterator = mock[java.util.Iterator[Event]]
                inSequence {
                  (mockIterator.hasNext _).expects().returning(true).once()
                  (mockIterator.hasNext _).expects().returning(false).once()
                }
                (mockIterator.next _).expects().returning(mockEvent)

                mockIterator
              })
              (mockEventSet.resume _).expects().once()
              mockEventSet
            })
            mockEventQueue
          })

          // Invoke the task function
          addTaskFunction.get.apply()

          // Should have not invoked handler for event
          isInvoked should be (false)
        }
      }
    }

    describe("#stop") {
      it("should throw an exception if not started") {
        intercept[IllegalArgumentException] {
          eventManager.stop()
        }
      }

      it("should remove the task processing events") {
        val taskId = java.util.UUID.randomUUID().toString
        (mockLoopingTaskRunner.addTask _).expects(*).returning(taskId).once()
        eventManager.start()

        // Ensure that removal of task is requested with proper id
        (mockLoopingTaskRunner.removeTask _).expects(taskId).once()
        eventManager.stop()
      }
    }

    describe("#addEventHandler") {
      it("should add to the existing collection of handlers") {
        val totalHandlers = 3
        val stubEventType = stub[EventType]

        val eventHandlers = (1 to totalHandlers)
          .map(_ => stub[EventManager#EventFunction])

        eventHandlers.foreach { eventHandler =>
          eventManager.addEventHandler(stubEventType, eventHandler)
        }

        eventManager.getEventHandlers(stubEventType) should
          contain theSameElementsAs eventHandlers
      }

      it("should become the first handler if no others exist for the event type") {
        val stubEventType = stub[EventType]
        val eventHandler = stub[EventManager#EventFunction]

        eventManager.addEventHandler(stubEventType, eventHandler)

        eventManager.getEventHandlers(stubEventType) should have length 1
        eventManager.getEventHandlers(stubEventType) should
          contain (eventHandler)
      }
    }

    describe("#getEventHandlers") {
      it("should return a collection of event handlers for the specific class") {
        val totalEventFunctions = 7
        val stubEventType = stub[EventType]

        (1 to totalEventFunctions).foreach(_ =>
          eventManager.addEventHandler(stubEventType, (_) => {}))

        // TODO: This is not working because eventMap.contains(class) is false
        //       even though eventMap.get(class) returns list
        eventManager.getEventHandlers(stubEventType) should
          have length totalEventFunctions
      }

      it("should return an empty collection if the class is not registered") {
        eventManager.getEventHandlers(stub[EventType]) should be (empty)
      }
    }

    describe("#removeEventHandler") {
      it("should do nothing if the function is not found in the list of handlers") {
        val stubEventType = stub[EventType]
        val stubEventHandler = stub[EventManager#EventFunction]

        eventManager.getEventHandlers(stubEventType) should be (empty)

        eventManager.removeEventHandler(stubEventType, stubEventHandler)

        eventManager.getEventHandlers(stubEventType) should be (empty)
      }

      it("should remove the handler if it exists for the specific event type") {
        val stubEventType = stub[EventType]
        val stubEventHandler = stub[EventManager#EventFunction]

        eventManager.addEventHandler(stubEventType, stubEventHandler)
        eventManager.getEventHandlers(stubEventType) should
          contain (stubEventHandler)

        eventManager.removeEventHandler(stubEventType, stubEventHandler)
        eventManager.getEventHandlers(stubEventType) should
          not contain stubEventHandler
      }
    }
  }
}
