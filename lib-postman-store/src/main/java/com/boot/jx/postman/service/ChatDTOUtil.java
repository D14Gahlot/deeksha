package com.boot.jx.postman.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.boot.jx.postman.PMConstants;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.ChatUserProfileDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.dto.ChatMessageDTO;
import com.boot.jx.postman.dto.ChatSessionDTO;
import com.boot.jx.postman.dto.ChatUserProfileDTO;
import com.boot.jx.postman.dto.ContactDTO;
import com.boot.jx.postman.model.ContactMeta;
import com.boot.jx.utils.PostManUtil;
import com.boot.utils.ArgUtil;
import com.boot.utils.EntityDtoUtil;
import com.boot.utils.NumberUtil;

public class ChatDTOUtil {

	public static ChatUserProfileDTO getProfileDTO(ChatUserProfileDoc profileDoc) {
		ChatUserProfileDTO dto = EntityDtoUtil.entityToDto(profileDoc, new ChatUserProfileDTO());
		return dto;
	}

	public static ContactMeta getContactMeta(ChatContactDoc chatContactDoc) {
		ContactMeta contact = new ContactMeta();
		contact.setName(chatContactDoc.getName());
		contact.setPhone(chatContactDoc.getPhone());
		contact.setEmail(chatContactDoc.getEmail());
		contact.setContactType(chatContactDoc.getContactType());
		return contact;
	}

	public static ContactDTO getContactDTO(ChatContactDoc chatContactDoc) {
		ContactDTO contact = new ContactDTO();

		if (ArgUtil.is(chatContactDoc)) {
			contact.setContactId(chatContactDoc.getContactId());
			contact.setContactType(chatContactDoc.getContactType());
			contact.setChannelType(chatContactDoc.getChannelType());
			contact.setName(chatContactDoc.getName());
			contact.setPhone(chatContactDoc.getPhone());
			contact.setEmail(chatContactDoc.getEmail());
			contact.setPhoneVerified(chatContactDoc.getPhoneVerified());
			contact.setEmailVerified(chatContactDoc.getEmailVerified());
			contact.setLabelId(chatContactDoc.getLabelId());
			contact.setProfilePic(chatContactDoc.getProfilePic());
			contact.setProfile(chatContactDoc.getProfile());
			contact.setLane(chatContactDoc.getLane());
			contact.setCsid(chatContactDoc.getCsid());

			contact.setCreatedBy(chatContactDoc.getCreatedBy());
			contact.setCreatedStamp(chatContactDoc.getCreatedStamp());
			contact.setLastInBoundStamp(chatContactDoc.getLastInBoundStamp());
			contact.setLastOutBoundStamp(chatContactDoc.getLastOutBoundStamp());
			contact.setLastOptInStamp(chatContactDoc.getLastOptInStamp());
			contact.setLastPushStamp(chatContactDoc.getLastPushStamp());
			contact.setLastReplyStamp(chatContactDoc.getLastReplyStamp());

			contact.setSessionId(chatContactDoc.getSessionId());
		}

		return contact;
	}

	public static List<ContactDTO> getContactDTO(List<ChatContactDoc> chatContactDocs) {
		return chatContactDocs.stream().map(chatContactDoc -> ChatDTOUtil.getContactDTO(chatContactDoc))
				.collect(Collectors.toList());
	}

	public static ChatMessageDTO getChatMessageDTO(MessageDoc messageDoc, String contactName, String defaultSender) {
		ChatMessageDTO messageDto = new ChatMessageDTO();
		if (!ArgUtil.is(messageDoc)) {
			return messageDto;
		}

		messageDto.setType(messageDoc.getType());
		messageDto.setText(messageDoc.getMessage());
		messageDto.setTemplate(messageDoc.getTemplate());
		messageDto.setTemplateId(messageDoc.getTemplateId());
		messageDto.setTimestamp(messageDoc.getTimestamp());
		messageDto.setSessionId(messageDoc.getSessionId());
		messageDto.setMessageId(messageDoc.getMessageId());
		messageDto.setMessageIdExt(messageDoc.getMessageIdExt());
		messageDto.setMessageIdRef(messageDoc.getMessageIdRef());

		messageDto.setReplyId(messageDoc.getReplyId());
		messageDto.setReplyIdExt(messageDoc.getReplyIdExt());

		messageDto.setTags(messageDoc.getTags());
		messageDto.setAttachments(messageDoc.getAttachments());
		messageDto.setVccards(messageDoc.getVccards());
		messageDto.setLogs(messageDoc.getLogs());
		messageDto.setAction(messageDoc.getAction());
		messageDto.setStatus(messageDoc.getStatus());
		messageDto.setStamps(messageDoc.getStamps());
		messageDto.setBulkSessionId(messageDoc.getBulkSessionId());
		messageDto.setMeta(messageDoc.getMeta());
		messageDto.setOptions(messageDoc.getOptions());
		messageDto.setReplyTo(messageDoc.getReplyTo());

		if (ArgUtil.is(messageDoc.getContact())) {
			messageDto.setContact(EntityDtoUtil.entityToDto(messageDoc.getContact(), new ContactDTO()));
		}

		if (ArgUtil.isEmpty(messageDto.getStamps()) && ArgUtil.is(messageDto.getStatus())) {
			Map<String, Long> stamps = new HashMap<String, Long>();
			stamps.put(messageDto.getStatus(), messageDto.getTimestamp());
			messageDto.setStamps(stamps);
		}

		messageDto.setRoute(messageDoc.getRoute());
		if (PostManUtil.isOutBound(messageDoc.getType())) {
			messageDto.setSender(ArgUtil.nonEmpty(messageDoc.route().getSenderCode(), messageDoc.route().getQueueCode(),
					messageDoc.getAgent(), messageDoc.getQueue(), defaultSender));
		} else if (PostManUtil.isInBound(messageDoc.getType())) {
			messageDto.setSender(ArgUtil.nonEmpty(messageDto.getName(), contactName));
		} else {
			messageDto.setSender(ArgUtil.nonEmpty(messageDoc.getAgent(), defaultSender));
		}

		if (ArgUtil.isEmpty(messageDto.getName())) {
			messageDto.setName(messageDto.getSender());
		}

		return messageDto;
	}

	public static ChatMessageDTO getChatMessageDTO(MessageDoc messageDoc) {
		if (!ArgUtil.is(messageDoc)) {
			return null;
		}
		return getChatMessageDTO(messageDoc, null, ArgUtil.nonEmpty(messageDoc.getAgent(), messageDoc.getQueue()));
	}

	public static List<ChatMessageDTO> getChatMessageDTO(List<MessageDoc> messageDocs, String contactName,
			String agentName) {
		List<ChatMessageDTO> messageDtos = new ArrayList<ChatMessageDTO>();
		for (MessageDoc messageDoc : messageDocs) {
			ChatMessageDTO messageDto = getChatMessageDTO(messageDoc, contactName, agentName);
			messageDtos.add(messageDto);
		}
		return messageDtos;
	}

	public static MessageDoc latestMessage(MessageDoc messageDoc1, MessageDoc messageDoc2) {
		if (!ArgUtil.is(messageDoc1)) {
			return messageDoc2;
		}
		if (!ArgUtil.is(messageDoc2)) {
			return messageDoc1;
		}

		if (messageDoc1.getTimestamp() > messageDoc2.getTimestamp()) {
			return messageDoc1;
		}
		return messageDoc2;
	}

	public static ChatSessionDTO getChatSessionDTO(ChatSessionDoc chatSessionDoc) {
		ChatSessionDTO chatSessionDto = EntityDtoUtil.entityToDto(chatSessionDoc, new ChatSessionDTO());
		chatSessionDto.setSessionId(chatSessionDto.getSessionId());
		chatSessionDto.setAssignedToAgent(chatSessionDoc.getAssignedToAgent());
		chatSessionDto.setAssignedToDept(chatSessionDoc.getAssignedToDept());
		chatSessionDto.setAssignedToBot(chatSessionDoc.getAssignedToBot());
		chatSessionDto.setActive(chatSessionDoc.isActive());
		chatSessionDto.setStatus(chatSessionDoc.getStatus());
		chatSessionDto.setName(chatSessionDoc.getContactName());

		chatSessionDto.setAssignedAgentStamp(chatSessionDoc.getAssignedAgentStamp());
		chatSessionDto.setAssignedDeptStamp(chatSessionDoc.getAssignedDeptStamp());
		chatSessionDto.setLastInComingStamp(chatSessionDoc.getLastInComingStamp());
		chatSessionDto.setLastResponseStamp(chatSessionDoc.getLastResponseStamp());

		if (ArgUtil.is(chatSessionDoc.getUpdated())) {
			chatSessionDto.setUpdatedStamp(chatSessionDoc.getUpdated().getStamp());
		} else {
			chatSessionDto.setUpdatedStamp(chatSessionDoc.getUpdatedStamp());
		}

		if (chatSessionDto.getAgentSessionStamp() == 0L) {
			chatSessionDto.setAgentSessionStamp(chatSessionDoc.getAssignedAgentStamp());
		}

		chatSessionDto.setUpdatedStamp(NumberUtil.max(chatSessionDto.getUpdatedStamp(),
				chatSessionDto.getLastInComingStamp(), chatSessionDto.getLastResponseStamp()));

		Map<String, ChatMessageDTO> msg = chatSessionDto.msg();

		if (!msg.containsKey("lastInBoundMsg")) {
			chatSessionDto.msg().put("lastInBoundMsg", getChatMessageDTO(chatSessionDoc.getLastInBoundMsg()));
		}
		if (!msg.containsKey("lastOutBoundMsg")) {
			chatSessionDto.msg().put("lastOutBoundMsg", getChatMessageDTO(chatSessionDoc.getLastOutBoundMsg()));
		}
		if (!msg.containsKey("lastMsg")) {
			chatSessionDto.msg().put("lastMsg", getChatMessageDTO(latestMessage(chatSessionDoc.getLastMsg(),
					latestMessage(chatSessionDoc.getLastInBoundMsg(), chatSessionDoc.getLastOutBoundMsg()))
			// Only Last Inoboud
			// ArgUtil.nonEmpty(chatSessionDoc.getLastOutBoundMsg(),chatSessionDoc.getLastMsg()))
			));
		}

		if (!ArgUtil.is(chatSessionDto.getStatus())) {
			if (chatSessionDto.isExpired()) {
				chatSessionDto.setStatus(PMConstants.CHAT_STATUS.EXPIRED.toString());
			} else if (!chatSessionDto.isActive()) {
				chatSessionDto.setStatus(PMConstants.CHAT_STATUS.CLOSED.toString());
			} else if (chatSessionDto.isResolved()) {
				chatSessionDto.setStatus(PMConstants.CHAT_STATUS.RESOLVED.toString());
			} else if (chatSessionDto.getAssignedAgentStamp() == 0) {
				chatSessionDto.setStatus(PMConstants.CHAT_STATUS.UNASSIGNED.toString());
			} else {
				chatSessionDto.setStatus(PMConstants.CHAT_STATUS.OPEN.toString());
			}
		}

		if (!ArgUtil.is(chatSessionDto.getState())) {
			if (chatSessionDto.isExpired()) {
				chatSessionDto.setState(PMConstants.CHAT_STATE.EXPIRED.toString());
			} else if (!chatSessionDto.isActive()) {
				chatSessionDto.setState(PMConstants.CHAT_STATE.CLOSED.toString());
			} else if (chatSessionDto.getAssignedAgentStamp() == 0) {
				chatSessionDto.setState(PMConstants.CHAT_STATE.UNATTENDED.toString());
			} else if (!chatSessionDto.isActive()) {
				chatSessionDto.setStatus(PMConstants.CHAT_STATE.ACTIVE.toString());
			}
		}

		return chatSessionDto;
	}

}
