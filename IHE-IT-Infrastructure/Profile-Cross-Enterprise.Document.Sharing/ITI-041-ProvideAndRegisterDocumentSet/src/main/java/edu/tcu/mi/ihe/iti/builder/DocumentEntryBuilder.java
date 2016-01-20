package edu.tcu.mi.ihe.iti.builder;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;

import edu.tcu.mi.ihe.constants.DocumentEntryConstants;
import edu.tcu.mi.ihe.constants.DocumentRelationshipsConstants;
import edu.tcu.mi.ihe.constants.EbXML;
import edu.tcu.mi.ihe.constants.Namespace;
import edu.tcu.mi.ihe.constants.ProvideAndRegistryDocumentSet_B_UUIDs;
import edu.tcu.mi.ihe.iti.model.Association;
import edu.tcu.mi.ihe.iti.model.Author;
import edu.tcu.mi.ihe.iti.model.DocumentEntry;
import edu.tcu.mi.ihe.utility.AxiomUtil;
import lombok.Setter;

public class DocumentEntryBuilder extends EntityBuilder {

	@Setter
	private DocumentEntry documentEntry;
	
	private Association lifecycle;

	public DocumentEntryBuilder(){
		objectType = ProvideAndRegistryDocumentSet_B_UUIDs.DOC_ENTRY_OBJECT;
	}

	public OMElement generateRelationship() {
//			// Relationship APND 11977
		if(documentEntry.getAppendDocumentId() != null) 
			createLifecycle(documentEntry.getAppendDocumentId(), DocumentRelationshipsConstants.APND);
//			// Relationship RPLC 11974
		if(documentEntry.getReplaceDocumentId() != null) 
			createLifecycle(documentEntry.getReplaceDocumentId(), DocumentRelationshipsConstants.RPLC);
//			// Relationship XFRM 11975
		if(documentEntry.getTransformDocumentId() != null) 
			createLifecycle(documentEntry.getTransformDocumentId(), DocumentRelationshipsConstants.XFRM);
//			// Relationship XFRM_RPLC 11976
		if(documentEntry.getTransformAndReplaceDocumentId() != null) 
			createLifecycle(documentEntry.getTransformAndReplaceDocumentId(), DocumentRelationshipsConstants.XFRM_RPLC);
//			// Relationship Signs 
		if(documentEntry.getSignatureDocumentId() != null) 
			createLifecycle(documentEntry.getSignatureDocumentId(), DocumentRelationshipsConstants.Signs);
		AssociationBuilder associationBuilder = new AssociationBuilder();
		associationBuilder.setAssociation(lifecycle);
		if(lifecycle != null)
			return associationBuilder.getMessageFromXML();
		return null;
	}
	
	private void createLifecycle(String docId, String associationType){
		lifecycle = new Association();
		lifecycle.setSourceObject(getId());
		lifecycle.setTargetObject(docId);
		lifecycle.setAssociation(associationType);
	}
	
	
	@Override
	public OMElement getMessageFromXML() {
		if(!validate()){
			return null;
		}
		AxiomUtil axiom = AxiomUtil.getInstance();
		OMElement root = axiom.createOMElement(EbXML.ExtrinsicObject, Namespace.RIM3);
		root.addAttribute("id", this.getId(), null);
		root.addAttribute("objectType", objectType, null);
		MetadataBuilder.objectRef.add(objectType);
		if(documentEntry.getMimeType() != null)
			root.addAttribute("mimeType", documentEntry.getMimeType(), null);// MimeType
		root.addAttribute("status", Namespace.APPROVED.getNamespace(), null);

		if(documentEntry.getCreationTime() == null)
			documentEntry.setCreationTime(generateTimeStamp());
		if(documentEntry.getCreationTime() != null) {
			OMElement slot = this.generateSlot(DocumentEntryConstants.CREATION_TIME, new String[] { documentEntry.getCreationTime() });
			root.addChild(slot);
		}

		String serviceStartTime = generateTimeStamp();
		if (serviceStartTime != null) {
			OMElement slot = this.generateSlot(DocumentEntryConstants.SERVICE_START_TIME, new String[] { serviceStartTime });
			root.addChild(slot);
		}
		String serviceStopTime = generateTimeStamp();
		if (serviceStopTime != null) {
			OMElement slot = this.generateSlot(DocumentEntryConstants.SERVICE_STOP_TIME, new String[] { serviceStopTime });
			root.addChild(slot);
		}
		String languageCode = "zh-tw";
		if (languageCode != null) {
			OMElement slot = this.generateSlot(DocumentEntryConstants.LANGUAGE_CODE, new String[] { languageCode });
			root.addChild(slot);
		}
		if (documentEntry.getPatient().getPatientId() != null) {
			OMElement slot = this.generateSlot(DocumentEntryConstants.SOURCE_PATIENT_ID, new String[] { documentEntry.getPatient().getPatientId() });
			root.addChild(slot);
		}
		
		PatientBuilder patientBuilder = new PatientBuilder();
		patientBuilder.setPatient(documentEntry.getPatient());
		if(documentEntry.getPatient() != null){
			OMElement pInfo = patientBuilder.getMessageFromXML();
			root.addChild(pInfo);
		}

		if (documentEntry.getContent() != null && !documentEntry.getContent().contains("http")) {
			if (documentEntry.getHash() != null) {
				OMElement slot = this.generateSlot(DocumentEntryConstants.HASH, new String[] { documentEntry.getHash().toString() });
				root.addChild(slot);
			}
			if (documentEntry.getSize() > 0) {
				OMElement slot = this.generateSlot(DocumentEntryConstants.SIZE, new String[] { documentEntry.getSize() + "" });
				root.addChild(slot);
			}
		} else {
			String[] url = extractURL(documentEntry.getContent());
			if (url != null) {
				OMElement slot = this.generateSlot(DocumentEntryConstants.URI, url);
				root.addChild(slot);
			}
		}
		// ---------------------Main
		if (documentEntry.getTitle() != null) {
			OMElement name = this.generateNameOrDescription(documentEntry.getTitle(), EbXML.Name);// Title
			root.addChild(name);
		}
		if (documentEntry.getDescription() != null) {
			OMElement name = this.generateNameOrDescription(documentEntry.getDescription(), EbXML.Description);
			root.addChild(name);
		}
		// ---------------------Author
		AuthorBuilder builder = new AuthorBuilder();
		for(Author author : documentEntry.getAuthors() ){
			builder.setAuthor(author);
			OMElement element = builder.getMessageFromXML();
			root.addChild(element);
		}
		// ---------------------Classification

		// ---ConfidentialityCode
		for(String value : documentEntry.getConfidentialityCode()){
			if (value != null) {
				value = value.trim();
				OMElement classification = generateClassification("confidentialityCode", value, DocumentEntryConstants.CODING_SCHEME, this.getId(), ProvideAndRegistryDocumentSet_B_UUIDs.DOC_ENTRY_CONFIDENTIALITY_CODE);
				if(classification != null)
					root.addChild(classification);
			}
		}
		// ---EventCodeList
		for(String value : documentEntry.getEventCodeList()){
			if (value != null) {
				value = value.trim();
				OMElement classification = generateClassification("eventCodeList", value, DocumentEntryConstants.CODING_SCHEME, this.getId(), ProvideAndRegistryDocumentSet_B_UUIDs.DOC_ENTRY_EVENT_CODE);
				if(classification != null)
					root.addChild(classification);
			}
		}
		// ---ClassCode
		if (documentEntry.getClassCode() != null) {
			OMElement classification = generateClassification("classCode", documentEntry.getClassCode().trim(), DocumentEntryConstants.CODING_SCHEME, this.getId(), ProvideAndRegistryDocumentSet_B_UUIDs.DOC_ENTRY_CLASS_CODE);
			if(classification != null)
				root.addChild(classification);
		}
		
		// ---FormatCode
		if (documentEntry.getFormatCode() != null) {
			OMElement classification = generateClassification("formatCode", documentEntry.getFormatCode().trim(), DocumentEntryConstants.CODING_SCHEME, this.getId(), ProvideAndRegistryDocumentSet_B_UUIDs.DOC_ENTRY_FORMAT_CODE);
			if(classification != null)
				root.addChild(classification);
		}
		// ---HealthcareFacilityTypeCode
		if (documentEntry.getHealthcareFacilityTypeCode() != null) {
			OMElement classification = generateClassification("healthcareFacilityTypeCode", documentEntry.getHealthcareFacilityTypeCode().trim(), DocumentEntryConstants.CODING_SCHEME, this.getId(), ProvideAndRegistryDocumentSet_B_UUIDs.DOC_ENTRY_HEALTH_CARE_FACILITY_CODE);
			if(classification != null)
				root.addChild(classification);
		}
		// ---PracticeSettingCode
		if (documentEntry.getPracticeSettingCode() != null) {
			OMElement classification = generateClassification("practiceSettingCode", documentEntry.getPracticeSettingCode().trim(), DocumentEntryConstants.CODING_SCHEME, this.getId(), ProvideAndRegistryDocumentSet_B_UUIDs.DOC_ENTRY_PRACTICE_SETTING_CODE);
			if(classification != null)
				root.addChild(classification);
		}
		// ---TypeCode
		if (documentEntry.getTypeCode() != null) {
			OMElement classification = generateClassification("typeCode", documentEntry.getTypeCode().trim(), DocumentEntryConstants.CODING_SCHEME, this.getId(), ProvideAndRegistryDocumentSet_B_UUIDs.DOC_ENTRY_TYPE_CODE);
			if(classification != null)
				root.addChild(classification);
		}

		// ---------------------ExternalIdentifier
		// ---PATIENT_ID
		OMElement name;
		name = generateNameOrDescription(DocumentEntryConstants.PATIENT_ID, EbXML.Name);
		OMElement externalIdentifier01 = generateExternalIdentifier(ProvideAndRegistryDocumentSet_B_UUIDs.DOC_ENTRY_PATIENT_IDENTIFICATION_SCHEME, getId(), documentEntry.getPatient().getPatientId(), name);
		root.addChild(externalIdentifier01);
		// ---UNIQUE_ID
		String uniqueId = MetadataBuilder.generateUniqueId();
		name = generateNameOrDescription(DocumentEntryConstants.UNIQUE_ID, EbXML.Name);
		OMElement externalIdentifier02 = generateExternalIdentifier(ProvideAndRegistryDocumentSet_B_UUIDs.DOC_ENTRY_UNIQUE_IDENTIFICATION_SCHEME, getId(), uniqueId, name);
		root.addChild(externalIdentifier02);
		return root;
	}

	@Override
	protected boolean validate() {
		if(documentEntry.getTitle() == null) {
			System.out.println("no title");
			return false;
		}
		int flag01 = documentEntry.getAppendDocumentId() != null? 1 : 0;
		int flag02 = documentEntry.getReplaceDocumentId() != null? 1 : 0;
		int flag03 = documentEntry.getTransformDocumentId() != null? 1 : 0;
		int flag04 = documentEntry.getTransformAndReplaceDocumentId() != null? 1 : 0;
		int flag05 = documentEntry.getSignatureDocumentId() != null? 1 : 0;
		int flag = flag01 + flag02 + flag03 + flag04 + flag05;

		if(!(flag == 0 || flag == 1)) {
			System.out.println("duplication exsting id");
			return false;
		}
		return true;
	}

	public OMElement generateDocument() {
		AxiomUtil axiom = AxiomUtil.getInstance();
		
		if (documentEntry.getContent() != null && documentEntry.getContent().contains("http")) {
			logger.info(documentEntry.getContent());
			return null;
		}

		OMElement root = axiom.createOMElement(EbXML.Document, Namespace.IHE);
		root.addAttribute("id", this.getId(), null);
		boolean swa = (documentEntry.getSoap() != null) ? documentEntry.getSoap().isSwa() : false;
		if (!swa) {
			root.setText(documentEntry.getContent());
		} else {
			OMNamespace xop = axiom.createNamespace("http://www.w3.org/2004/08/xop/include", "xop");
			OMElement inclue = axiom.createOMElement("Include", xop);
			inclue.addAttribute("href", documentEntry.getContent(), null);
			root.addChild(inclue);
		}
		return root;
	}
	
	private String[] extractURL(String content) {
		if(content == null) return null;
		int block = 128;
		int size = (content.length() / 128) + 1;
		String[] token = new String[size];
		for (int i = 0; i < token.length; i++) {
			if ((i + 1) * block <= content.length())
				token[i] = (i+1) + "|" + content.substring(i * block, (i + 1) * block);
			else
				token[i] = (i+1) + "|" + content.substring(i * block);
		}
		return token;
	}

	@Override
	public String getMessageFromHL7v2() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getId() {
		return documentEntry.getId();
	}
}
