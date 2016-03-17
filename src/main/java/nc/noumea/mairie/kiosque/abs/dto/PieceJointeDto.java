package nc.noumea.mairie.kiosque.abs.dto;

import java.io.Serializable;

public class PieceJointeDto implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6418009304707574279L;
	

	private Integer idPieceJointe;
	private byte[] bFile;
	private String typeFile;
	private String titre;
	private String urlFromAlfresco;
	private String nodeRefAlfresco;
	
	
	public Integer getIdPieceJointe() {
		return idPieceJointe;
	}
	public void setIdPieceJointe(Integer idPieceJointe) {
		this.idPieceJointe = idPieceJointe;
	}
	public byte[] getbFile() {
		return bFile;
	}
	public void setbFile(byte[] bFile) {
		this.bFile = bFile;
	}
	public String getTypeFile() {
		return typeFile;
	}
	public void setTypeFile(String typeFile) {
		this.typeFile = typeFile;
	}
	public String getTitre() {
		return titre;
	}
	public void setTitre(String titre) {
		this.titre = titre;
	}
	public String getUrlFromAlfresco() {
		return urlFromAlfresco;
	}
	public void setUrlFromAlfresco(String urlFromAlfresco) {
		this.urlFromAlfresco = urlFromAlfresco;
	}
	public String getNodeRefAlfresco() {
		return nodeRefAlfresco;
	}
	public void setNodeRefAlfresco(String nodeRefAlfresco) {
		this.nodeRefAlfresco = nodeRefAlfresco;
	}

}
