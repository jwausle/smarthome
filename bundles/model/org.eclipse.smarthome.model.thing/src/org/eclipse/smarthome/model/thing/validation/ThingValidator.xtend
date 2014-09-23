/*
 * generated by Xtext
 */
package org.eclipse.smarthome.model.thing.validation

import org.eclipse.smarthome.model.thing.thing.ModelThing
import org.eclipse.xtext.validation.Check
import org.eclipse.smarthome.model.thing.thing.ThingPackage
import org.eclipse.xtext.nodemodel.util.NodeModelUtils
import org.eclipse.smarthome.core.thing.ThingUID
import javax.inject.Inject
import org.eclipse.xtext.serializer.ISerializer
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.resource.XtextResource

/**
 * Custom validation rules. 
 *
 * see http://www.eclipse.org/Xtext/documentation.html#validation
 */
class ThingValidator extends AbstractThingValidator {

  public static val INVALID_NAME = 'invalidName'

	@Check
	def check_thing_has_valid_id(ModelThing thing) {
		if (thing.nested) {
			// We have to provide thingTypeId and a thingId
			if (!thing.eIsSet(ThingPackage.Literals.MODEL_THING__THING_TYPE_ID)) {
				if (thing.eIsSet(ThingPackage.Literals.MODEL_PROPERTY_CONTAINER__ID)) {
					error("Provide a thing type ID and a thing ID in this format:\n <thingTypeId> <thingId>", ThingPackage.Literals.MODEL_PROPERTY_CONTAINER__ID)
				} else {
					if (thing.eIsSet(ThingPackage.Literals.MODEL_BRIDGE__BRIDGE)) {
						error("Provide a thing type ID and a thing ID in this format:\n <thingTypeId> <thingId>", ThingPackage.Literals.MODEL_BRIDGE__BRIDGE)
					}
				}
			} else {
				if (!thing.eIsSet(ThingPackage.Literals.MODEL_THING__THING_ID)) {
					error("Provide a thing type ID and a thing ID in this format:\n <thingTypeId> <thingId>", ThingPackage.Literals.MODEL_THING__THING_TYPE_ID)
				}
			}
		} else { // thing in container 
			if (thing.eIsSet(ThingPackage.Literals.MODEL_THING__THING_TYPE_ID) && thing.eIsSet(ThingPackage.Literals.MODEL_THING__THING_ID)) {
				val thingTypeIdFeature = NodeModelUtils.findNodesForFeature(thing, ThingPackage.Literals.MODEL_THING__THING_TYPE_ID).head
				val thingIdFeature = NodeModelUtils.findNodesForFeature(thing, ThingPackage.Literals.MODEL_THING__THING_ID).head
				val startOffset = thingTypeIdFeature.offset
				val endOffset = thingIdFeature.endOffset
				getMessageAcceptor().acceptError("Provide a thing UID in this format:\n <bindingId>:<thingTypeId>:<thingId>", thing, startOffset, endOffset - startOffset, null, null)
			} else {
				if (thing.id != null) {
					try {
						new ThingUID(thing.id)
					} catch (IllegalArgumentException e) {
						error(e.message, ThingPackage.Literals.MODEL_PROPERTY_CONTAINER__ID)
					}
				}
			}
		}

	}
	
	def private isNested(ModelThing thing) {
		thing.eContainingFeature == ThingPackage.Literals.MODEL_BRIDGE__THINGS
	}
}
