package org.eclipse.smarthome.automation.core.internal.composite;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.handler.ConditionHandler;
import org.eclipse.smarthome.automation.type.CompositeConditionType;

public class CompositeConditionHandler extends
        AbstractCompositeModuleHandler<Condition, CompositeConditionType, ConditionHandler>implements ConditionHandler {

    public CompositeConditionHandler(Condition condition, CompositeConditionType mt,
            LinkedHashMap<Condition, ConditionHandler> mapModuleToHandler, String ruleUID) {
        super(condition, mt, mapModuleToHandler);
    }

    @Override
    public boolean isSatisfied(Map<String, ?> context) {
        Map<String, Object> internalContext = new HashMap<>(context);
        List<Condition> children = moduleType.getModules();
        for (Condition child : children) {
            Map<String, ?> compositeContext = getCompositeContext(internalContext, module);
            Map<String, Object> originalConfig = new HashMap<>(child.getConfiguration());
            updateChildConfig(child, compositeContext);
            ConditionHandler childHandler = moduleHandlerMap.get(child);
            boolean isSatisfied = childHandler.isSatisfied(compositeContext);
            child.setConfiguration(originalConfig); // restore original configs with links

            if (!isSatisfied) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
