package me.alexeyterekhov.vkfilter.Data.UpdateEvents

import me.alexeyterekhov.vkfilter.Data.Entities.Dialog.DialogId
import me.alexeyterekhov.vkfilter.Data.Entities.Message.Message

class EventMessagesRead(val dialogId: DialogId, val readMessages: Collection<Message>)