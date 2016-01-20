package edu.tcu.mi.ihe.iti.builder;

import org.apache.axiom.om.OMElement;

import edu.tcu.mi.ihe.constants.EbXML;
import edu.tcu.mi.ihe.constants.Namespace;
import edu.tcu.mi.ihe.constants.ProvideAndRegistryDocumentSet_B_UUIDs;
import edu.tcu.mi.ihe.constants.SubmissionSetConstants;
import edu.tcu.mi.ihe.iti.model.Author;
import edu.tcu.mi.ihe.iti.model.SubmissionSet;
import edu.tcu.mi.ihe.utility.AxiomUtil;
import lombok.Setter;

public class SubmissionSetBuilder extends EntityBuilder {
	@Setter
	private SubmissionSet submissionSet; 

	public SubmissionSetBuilder(){
		objectType = ProvideAndRegistryDocumentSet_B_UUIDs.SUBMISSON_SET_OBJECT;
	}
	
	@Override
	public OMElement getMessageFromXML() {
		if(!validate()){
			logger.info("validate error");
			return null;
		}
		AxiomUtil axiom = AxiomUtil.getInstance();
		OMElement root = axiom.createOMElement(EbXML.RegistryPackage, Namespace.RIM3);
		root.addAttribute("id", this.getId(), null);
		root.addAttribute("objectType", objectType, null);
		MetadataBuilder.objectRef.add(objectType);
		// --submissionTime
		String submissionTime = generateTimeStamp();
		if (submissionTime != null) {
			OMElement slot = this.generateSlot(SubmissionSetConstants.SUBMISSION_TIME, new String[] { submissionTime });
			root.addChild(slot);
		}
		// ---------------------Author
		AuthorBuilder authorBuilder = new AuthorBuilder();
		for(Author author : submissionSet.getAuthors()){
			authorBuilder.setAuthor(author);
			OMElement element = authorBuilder.getMessageFromXML();
			if(element != null)
				root.addChild(element);
		}
		// ---ContentTypeCode
		if (submissionSet.getContentTypeCode() != null) {
			OMElement element = generateClassification("contentTypeCode", submissionSet.getContentTypeCode().trim(), SubmissionSetConstants.CODING_SCHEME, this.getId(), ProvideAndRegistryDocumentSet_B_UUIDs.SUBMISSION_SET_CONTENT_TYPE_CODE);
			if(element != null)
				root.addChild(element);
		}
		
		// ---------------------ExternalIdentifier
		OMElement name;
		name = generateNameOrDescription(SubmissionSetConstants.PATIENT_ID, EbXML.Name);
		OMElement externalIdentifier01 = generateExternalIdentifier(ProvideAndRegistryDocumentSet_B_UUIDs.SUBMISSION_SET_PATIENT_IDENTIFICATION_SCHEME, this.getId(), submissionSet.getPatient().getPatientId(), name);
		root.addChild(externalIdentifier01);
		
		String uniqueId = MetadataBuilder.generateUniqueId();
		name = generateNameOrDescription(SubmissionSetConstants.UNIQUE_ID, EbXML.Name);
		OMElement externalIdentifier02 = generateExternalIdentifier(ProvideAndRegistryDocumentSet_B_UUIDs.SUBMISSION_SET_UNIQUE_IDENTIFICATION_SCHEME, this.getId(), uniqueId, name);
		root.addChild(externalIdentifier02);
		
		name = generateNameOrDescription(SubmissionSetConstants.SOURCE_ID, EbXML.Name);
		OMElement externalIdentifier03 = generateExternalIdentifier(ProvideAndRegistryDocumentSet_B_UUIDs.SUBMISSION_SET_SOURCE_IDENTIFICATION_SCHEME, this.getId(), MetadataBuilder.SourceID, name);
		root.addChild(externalIdentifier03);
		return root;
	}

	@Override
	protected boolean validate() {
		if(MetadataBuilder.SourceID == null || MetadataBuilder.SourceID.equals(""))
			return false;
		return true;
	}
	
	@Override
	public String getMessageFromHL7v2() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getId() {
		return submissionSet.getId();
	}

}
