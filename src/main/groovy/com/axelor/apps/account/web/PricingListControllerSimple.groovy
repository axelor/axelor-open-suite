package com.axelor.apps.account.web

import groovy.util.logging.Slf4j

import com.axelor.apps.account.db.InvoiceLine
import com.axelor.apps.account.db.MatrixStructure
import com.axelor.apps.account.db.PricingList
import com.axelor.apps.account.db.PricingListVersion
import com.axelor.db.Model
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.axelor.rpc.Context
import com.google.inject.persist.Transactional

@Slf4j
class PricingListControllerSimple
{
    /**
     * Initialise la version de barème.
     * 
     * @param ActionRequest  request
     * @param ActionResponse response
     */
    void initVersion(ActionRequest request, ActionResponse response)
    {
        response.attrs = this.globalInit(request.context, "version")
    }

    /**
     * Initialise la ligne de barème.
     *
     * @param ActionRequest  request
     * @param ActionResponse response
     */
    void initLine(ActionRequest request, ActionResponse response)
    {
        if (request.context.parentContext) {
            response.attrs = this.globalInit(request.context.parentContext, "line")
        }
    }

    /**
     * Initialise les deux points d'entrées.
     *
     * @param  Context versionContext Le contexte de la version de barème
     * @param  String  from           La vue actuelle
     * 
     * @return Map     Les attributs de la vue actuelle
     */
    Map globalInit(Context versionContext, String from)
    {
        def parentMap = this.getParents(versionContext)

        if (parentMap) {
            def pricingLineList        = this.findPricingLineList(parentMap)
            def unifiedMatrixStructure = this.buildUnifiedMatrixStructure(pricingLineList)

            return this.setAttributes(unifiedMatrixStructure, from)
        }

        return new HashMap()
    }

    /**
     * Récupère les objets parent et grand-parent de la version de barème pour
     * déterminer le point d'entrée (barème|tarif|contrat).
     *
     * @param  Context versionContext Le contexte de la version de barème
     *
     * @return Map     Le barème et son parent s'il existe
     */
    Map getParents(Context versionContext)
    {
		PricingListVersion pricingListVersion = versionContext as PricingListVersion
        Context parent                        = versionContext.parentContext
        Map parentMap                         = new HashMap<String, Model>()

        if (parent) {
            // Venant d'un barème
            if ("com.axelor.apps.pricing.db.PricingList" == parent._model) {
                PricingList pricingList = parent as PricingList
                Context grandparent     = parent.parentContext
                parentMap.put("pricingList", pricingList)

                if (grandparent) {
                    // Venant de la vue barème par composante
                    if ("com.axelor.apps.contract.db.PricingByConstituent" == grandparent._model) {
//                        PricingByConstituent pricingByConstituent = grandparent as PricingByConstituent
//                        parentMap.put("pricingByConstituent", pricingByConstituent)
                    }
                    // Venant de la vue ligne de tarif
                    else if ("com.axelor.apps.pricing.db.PricingLine" == grandparent._model) {
//                        PricingLine pricingLine = grandparent as PricingLine
//                        parentMap.put("pricingLine", pricingLine)
                    }
                }
            }
            // Venant de la vue ligne de facture
            else if ("com.axelor.apps.account.db.InvoiceLine" == parent._model) {
                InvoiceLine invoiceLine = parent as InvoiceLine
                parentMap.put("invoiceLine", invoiceLine)
            }
        }

        return parentMap
    }

    /**
     * Retourne les lignes de barèmes ou les barèmes par composantes en fonction
     * des parents de la version de barème.
     *
     * @param  Map  parentMap Le parent et le grand-parent de la version de barème
     *
     * @return List Lignes de barèmes ou barèmes par composantes (tarif)
     */
    List findPricingLineList(Map parentMap)
    {
        def pricingLineList

        // Venant de la vue tarif
        if (parentMap["pricingLine"])
        {
            pricingLineList = PricingLine.all().filter(
                "pricingList = ?1 AND pricing = ?2",
                parentMap["pricingList"], parentMap["pricing"]
            ).fetch()
        }
        // Venant de la vue barème
        else
        {
            pricingLineList = PricingLine.all().filter(
                "pricingList = ?1", parentMap["pricingList"]
            ).fetch()
        }

        return pricingLineList?.size() > 0 ? pricingLineList : null
    }

    /**
     * Construit un masque de barème "unifié" qui regroupe les masques de barème
     * d'une liste de lignes de tarifs.
     *
     * @param  List            pricingLineList Liste de lignes de tarifs
     *
     * @return MatrixStructure Le masque de barème unifié
     */
    MatrixStructure buildUnifiedMatrixStructure(List pricingLineList)
    {
        def unifiedMatrixStructure = new MatrixStructure()
        def matrixStructureSet     = []
        def hasAttr                = false

        if (pricingLineList)
        {
            for (line in pricingLineList)
            {
                matrixStructureSet = line.pricingStructureLine?.constituent?.matrixStructureSet

                if (matrixStructureSet)
                {

                    for (matrixStructure in matrixStructureSet)
                    {
                        for (i in 1..10)
                        {
                            if (!unifiedMatrixStructure."param$i" && matrixStructure."param$i")
                            {
                                hasAttr = true
                                unifiedMatrixStructure."param$i" = matrixStructure."param$i"
                            }
                        }
                        for (i in 1..5)
                        {
                            if (!unifiedMatrixStructure."valueName$i" && matrixStructure."valueName$i")
                            {
                                hasAttr = true
                                unifiedMatrixStructure."valueName$i" = matrixStructure."valueName$i"
                            }
                        }
                    }
                }
            }
        }

        return hasAttr ? unifiedMatrixStructure : null
    }

    /**
     * Définit les attributs pour les paramètres des lignes de barèmes de la vue
     * en cours.
     *
     * @param  MatrixStructure unifiedMatrixStructure Le masque de barème unifié
     * @param  String          from                   Le point d'entrée
     *
     * @return Map             Les attributs pour la vue en cours
     */
    Map setAttributes(MatrixStructure unifiedMatrixStructure, String from)
    {
        def attrName = "version" == from ? "pricingListLineList." : ""
        def attrs    = [:]

        if (unifiedMatrixStructure)
        {
            for (i in 1..8)
            {
                if (unifiedMatrixStructure."param$i")
                {
                    attrs << ["${attrName}parameter$i": [
                        title: unifiedMatrixStructure."param$i".name,
                        domain: String.format("self.parameterType.name = '%1s'",
                            unifiedMatrixStructure."param$i".name),
                        hidden: false,
                    ]]
                }
                else
                {
                    attrs << ["${attrName}parameter$i": [
                        title: "Paramètre $i",
                        domain: null,
                        hidden: true,
                    ]]
                }
            }
            for (i in 9..10)
            {
                if (unifiedMatrixStructure."param$i")
                {
                    attrs << ["${attrName}parameter$i": [
                        title: unifiedMatrixStructure."param$i",
                        hidden: false,
                    ]]
                }
                else
                {
                    attrs << ["${attrName}parameter$i": [
                        title: "Paramètre $i",
                        hidden: true,
                    ]]
                }
            }

            for (i in 1..5)
            {
                if (unifiedMatrixStructure."valueName$i")
                {
                    attrs << ["${attrName}val$i": [
                        title: unifiedMatrixStructure."valueName$i",
                        hidden: false,
                    ]]
                }
                else
                {
                    attrs << ["${attrName}val$i": [
                        title: "Valeur $i",
                        hidden: true,
                    ]]
                }
            }
        }
        else
        {
            for (i in 1..8)
            {
                attrs << ["${attrName}parameter$i": [
                    title: "Paramètre $i",
                    domain: null,
                    hidden: false,
                ]]
            }
            for (i in 9..10)
            {
                attrs << ["${attrName}parameter$i": [
                    title: "Paramètre $i",
                    hidden: false,
                ]]
            }

            for (i in 1..5)
            {
                attrs << ["${attrName}val$i": [
                    title: "Valeur $i",
                    hidden: false,
                ]]
            }
        }

        return attrs
    }

    /**
     * Ajuste les dates de la liste des versions de barèmes.
     *
     * Pour chaque version de barème de la liste :
     *
     *   - SI la version est active ALORS met la date de fin de la précédente
     *     version à la date de début de la version actuelle - 1 jour
     *
     *   - SINON met la date de fin de la précédente version à null
     *
     * @param ActionRequest  request
     * @param ActionResponse response
     */
    void fixVersionListDates(ActionRequest request, ActionResponse response)
    {
		PricingList pricingList = request.context as PricingList
		PricingListVersion pricingListVersion = pricingList?.pricingListVersionList?.find{it.selected == true}
		
		response.setValues([
			pricingListVersionList: this.ajustDates(pricingList.pricingListVersionList, pricingListVersion)
			])

    }
	
	
	@Transactional
	void fixVersionListDatesFromPricingListVersion(ActionRequest request, ActionResponse response)
	{
		PricingListVersion pricingListVersion = request.context as PricingListVersion
		
		List<PricingListVersion> allVersionList = new ArrayList<PricingListVersion>()
		
		if(pricingListVersion.id){
			allVersionList = PricingListVersion.all().filter("self.pricingList = ?1 and self.id != ?2", pricingListVersion.pricingList,pricingListVersion.id).fetch()
		}
		else{
			allVersionList = PricingListVersion.all().filter("self.pricingList = ?1", pricingListVersion.pricingList).fetch()
		}
		
		allVersionList.add(pricingListVersion)
		
		for (PricingListVersion version : this.ajustDates(allVersionList, pricingListVersion)) {
			if(version.id != null && !version?.equals(pricingListVersion)){
				version.save()
			}
		}

		response.setValues(pricingListVersion)

	}
	
	List<PricingListVersion> ajustDates(List<PricingListVersion> pricingListVersionList, PricingListVersion pricingListVersion){
		
		// Comparator : Short by fromDate
		def compareByFromDate = [ compare: { a, b ->
			a.fromDate.equals(b.fromDate) ? 0 : a.fromDate < b.fromDate ? -1 : 1
		}] as Comparator
	
		// Comparator : Short by -fromDate
		def reverseComparator = Collections.reverseOrder(compareByFromDate)
		
		Collections.sort(pricingListVersionList, reverseComparator)

		PricingListVersion tempVersion

		for (PricingListVersion version : pricingListVersionList)
		{
			// Si la précédente version de la liste inversée était active
			if (tempVersion?.activeOk)
			{
				// La date de fin de la version actuelle prend la date de début
				// de la précédente - 1 jour
				version.toDate = tempVersion.fromDate.minusDays(1)
			}
			else
			{
				// Sinon on la met à null
				version.toDate = null
			}

			tempVersion = version
		}

		// On remet la liste dans l'ordre chronologique
		Collections.sort(pricingListVersionList, compareByFromDate)
		
		//On désactive toutes les versions de barèmes suivant celle selectionné et passé de active à non active
		boolean isAfter = false
		
		for (PricingListVersion version : pricingListVersionList){
			if(version?.equals(pricingListVersion)){
				isAfter = true
				if(!pricingListVersion.activeOk){
					version.toDate = null
				}
				continue;
			}

			if(isAfter && !pricingListVersion?.activeOk){
				version.activeOk = false
				version.toDate = null
			}
		
		}
		
		return pricingListVersionList
	}
}