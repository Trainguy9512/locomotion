package com.trainguy9512.locomotion.animation.joint.skeleton;

import com.google.common.collect.Maps;
import com.trainguy9512.locomotion.animation.joint.JointChannel;
import net.minecraft.client.model.geom.PartPose;
import org.apache.commons.compress.utils.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Structure used for associating locator enums with data such as transform hierarchy, default offset poses, model parts, and mirrors.
 */
public class JointSkeleton {

    private static final Logger LOGGER = LogManager.getLogger("Locomotion.JointSkeleton");

    private final Map<String, JointConfiguration> joints;
    private final String rootJoint;

    private JointSkeleton(Map<String, JointConfiguration> joints, String rootJoint){
        this.joints = joints;
        this.rootJoint = rootJoint;
    }

    /**
     * Returns a new Joint Skeleton builder.
     * @param rootJoint Name of the joint to use as the root.
     * @return Joint skeleton builder
     */
    public static JointSkeleton.Builder of(String rootJoint) {
        return new JointSkeleton.Builder(rootJoint);
    }

    /**
     * Returns a list of joint identifiers that are direct children of the supplied joint.
     * @param joint Joint to search for children of.
     * @return List of
     */
    public List<String> getDirectChildrenOfJoint(String joint) {
        LOGGER.info(this.rootJoint);
        return this.joints.get(joint).children();
    }

    /**
     * Returns whether the supplied parent joint is a parent of the supplied child joint.
     * @param parent Parent joint identifier
     * @param child Child joint identifier
     */
    public boolean jointIsParentOfChild(String parent, String child) {
        return Objects.equals(this.joints.get(child).parent(), parent);
    }

    @SuppressWarnings("unused")
    public void printHierarchy() {
        printHierarchyChild(this.getRootJoint(), 1);
        LOGGER.info("--".concat(this.getRootJoint()));
    }

    private void printHierarchyChild(String joint, int size) {
        String dashes = "";
        for(int i = 0; i <= size; i++){
            dashes = dashes.concat("--");
        }
        String finalDashes = dashes;
        this.getDirectChildrenOfJoint(joint).forEach(child -> {
            LOGGER.info(finalDashes.concat(child));
            printHierarchyChild(child, size + 1);
        });
    }

    /**
     * Retrieves the root joint of the skeleton.
     * @return String identifier of the root joint
     */
    public String getRootJoint() {
        return this.rootJoint;
    }

    /**
     * Returns a set of all joints used by the joint skeleton.
     * @return Set of string joint identifiers
     */
    public Set<String> getJoints() {
        return joints.keySet();
    }

    /**
     * Retrieves the joint configuration for the supplied joint.
     * @param joint Joint string identifier to get a joint configuration for.
     * @return Joint configuration for the supplied joint string identifier
     */
    public JointConfiguration getJointConfiguration(String joint) {
        return this.joints.get(joint);
    }

    public boolean containsJoint(String joint) {
        return this.joints.containsKey(joint);
    }

    public static class Builder {

        private final Map<String, JointConfiguration> joints = Maps.newHashMap();
        private final String rootJoint;

        protected Builder(String rootJoint) {
            this.rootJoint = rootJoint;
        }

        public Builder defineJoint(String jointName, JointConfiguration jointConfiguration) {
            this.joints.put(jointName, jointConfiguration);
            return this;
        }

        public JointSkeleton build(){
            HashMap<String, JointConfiguration> jointsBuilt = Maps.newHashMap();
            return new JointSkeleton(this.joints, this.rootJoint);
        }
    }


    public record JointConfiguration(
            String parent,
            List<String> children,
            JointChannel referencePose,
            String mirrorJoint,
            String modelPartIdentifier,
            PartPose modelPartOffset
    ) {

        public static Builder builder(){
            return new Builder();
        }

        public static class Builder {
            private String parent;
            private final List<String> children;
            private JointChannel referencePose;
            private String mirrorJoint;
            private String modelPartIdentifier;
            private PartPose modelPartOffset;

            private Builder(){
                this.parent = null;
                this.children = new ArrayList<>();
                this.mirrorJoint = null;
                this.modelPartIdentifier = null;
                this.modelPartOffset = PartPose.ZERO;
            }

            public Builder addChild(String child){
                this.children.add(child);
                return this;
            }

            public Builder setParent(String parent){
                this.parent = parent;
                return this;
            }

            public Builder setReferencePose(JointChannel referencePose) {
                this.referencePose = referencePose;
                return this;
            }

            public Builder setMirrorJoint(String mirrorJoint){
                this.mirrorJoint = mirrorJoint;
                return this;
            }

            public Builder setModelPartIdentifier(String modelPartIdentifier){
                this.modelPartIdentifier = modelPartIdentifier;
                return this;
            }

            public Builder setModelPartOffset(PartPose modelPartOffset){
                this.modelPartOffset = modelPartOffset;
                return this;
            }

            public JointConfiguration build(){
                return new JointConfiguration(this.parent, this.children, this.referencePose, this.mirrorJoint, this.modelPartIdentifier, this.modelPartOffset);
            }
        }
    }
}
