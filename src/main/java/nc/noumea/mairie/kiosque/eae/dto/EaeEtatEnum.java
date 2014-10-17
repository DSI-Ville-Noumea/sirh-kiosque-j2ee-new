package nc.noumea.mairie.kiosque.eae.dto;

public enum EaeEtatEnum {
	
	NA("Non affecté"),
	ND("Non débuté"),
	C("Créé"),
	EC("En cours"),
	F("Finalisé"),
	CO("Contrôlé"),
	S("Supprimé");
	
	private String statut;
	
	private EaeEtatEnum(String _statut) {
		this.statut = _statut;
	}
	
	@Override
	public String toString() {
		return statut;
	}
}