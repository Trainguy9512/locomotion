package com.trainguy9512.locomotion.animation.pose.function;

import com.trainguy9512.locomotion.animation.joint.JointChannel;
import com.trainguy9512.locomotion.animation.joint.Transformer;
import com.trainguy9512.locomotion.animation.pose.Pose;
import com.trainguy9512.locomotion.animation.pose.ComponentSpacePose;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class JointTransformerFunction<P extends Pose> implements PoseFunction<P> {

    private final PoseFunction<P> input;
    private final String joint;
    private final TransformChannelConfiguration<Vector3f> translationConfiguration;
    private final TransformChannelConfiguration<Quaternionf> rotationConfiguration;
    private final TransformChannelConfiguration<Vector3f> scaleConfiguration;
    private final TransformChannelConfiguration<Matrix4f> matrixConfiguration;
    private final Function<FunctionInterpolationContext, Float> weightFunction;

    private JointTransformerFunction(PoseFunction<P> input, String joint, TransformChannelConfiguration<Vector3f> translationConfiguration, TransformChannelConfiguration<Quaternionf> rotationConfiguration, TransformChannelConfiguration<Vector3f> scaleConfiguration, TransformChannelConfiguration<Matrix4f> matrixConfiguration, Function<FunctionInterpolationContext, Float> weightFunction) {
        this.input = input;
        this.joint = joint;
        this.translationConfiguration = translationConfiguration;
        this.rotationConfiguration = rotationConfiguration;
        this.scaleConfiguration = scaleConfiguration;
        this.matrixConfiguration = matrixConfiguration;
        this.weightFunction = weightFunction;
    }

    private static <P extends Pose> JointTransformerFunction<P> of(Builder<P> builder){
        return new JointTransformerFunction<>(builder.input, builder.joint, builder.translationConfiguration, builder.rotationConfiguration, builder.scaleConfiguration, builder.matrixConfiguration, builder.weightFunction);
    }

    @Override
    public @NotNull P compute(FunctionInterpolationContext context) {
        if (!context.driverContainer().getJointSkeleton().containsJoint(this.joint)) {
            throw new IllegalArgumentException("Cannot run joint transformer function on joint " + this.joint + ", for it is not present within the skeleton. Valid joints: " + context.driverContainer().getJointSkeleton().getJoints());
        }

        P pose = this.input.compute(context);
        float weight = this.weightFunction.apply(context);

        JointChannel jointChannel = pose.getJointChannel(this.joint);
        this.transformJoint(jointChannel, context, this.translationConfiguration, JointChannel::translate);
        this.transformJoint(jointChannel, context, this.rotationConfiguration, JointChannel::rotate);
        this.transformJoint(jointChannel, context, this.scaleConfiguration, JointChannel::scale);
        this.transformJoint(jointChannel, context, this.matrixConfiguration, JointChannel::multiply);

        if(weight != 0){
            if(weight == 1){
                pose.setJointChannel(this.joint, jointChannel);
            } else {
                pose.setJointChannel(this.joint, pose.getJointChannel(this.joint).interpolate(jointChannel, weight));
            }
        }
        return pose;
    }

    private <X> void transformJoint(JointChannel jointChannel, FunctionInterpolationContext context, TransformChannelConfiguration<X> configuration, Transformer<X> transformer){
        transformer.transform(jointChannel, configuration.transformFunction.apply(context), configuration.transformSpace, configuration.transformType);
    }

    @Override
    public void tick(FunctionEvaluationState evaluationState) {
        this.input.tick(evaluationState);
    }

    @Override
    public PoseFunction<P> wrapUnique() {
        return new JointTransformerFunction<>(this.input.wrapUnique(), this.joint, this.translationConfiguration, this.rotationConfiguration, this.scaleConfiguration, this.matrixConfiguration, this.weightFunction);
    }

    @Override
    public Optional<PoseFunction<?>> searchDownChainForMostRelevant(Predicate<PoseFunction<?>> findCondition) {
        return findCondition.test(this) ? Optional.of(this) : this.input.searchDownChainForMostRelevant(findCondition);
    }

    public static Builder<LocalSpacePose> localOrParentSpaceBuilder(PoseFunction<LocalSpacePose> poseFunction, String joint){
        return new Builder<>(poseFunction, joint);
    }

    public static Builder<ComponentSpacePose> componentSpaceBuilder(PoseFunction<ComponentSpacePose> poseFunction, String joint){
        return new Builder<>(poseFunction, joint);
    }

    public static class Builder<P extends Pose> {

        private final PoseFunction<P> input;
        private final String joint;
        private TransformChannelConfiguration<Vector3f> translationConfiguration;
        private TransformChannelConfiguration<Quaternionf> rotationConfiguration;
        private TransformChannelConfiguration<Vector3f> scaleConfiguration;
        private TransformChannelConfiguration<Matrix4f> matrixConfiguration;
        private Function<FunctionInterpolationContext, Float> weightFunction;

        private Builder(PoseFunction<P> poseFunction, String joint){
            this.joint = joint;
            this.input = poseFunction;
            this.translationConfiguration = TransformChannelConfiguration.of((context) -> new Vector3f(0), JointChannel.TransformType.IGNORE, JointChannel.TransformSpace.LOCAL);
            this.rotationConfiguration = TransformChannelConfiguration.of((context) -> new Quaternionf().identity(), JointChannel.TransformType.IGNORE, JointChannel.TransformSpace.LOCAL);
            this.scaleConfiguration = TransformChannelConfiguration.of((context) -> new Vector3f(0), JointChannel.TransformType.IGNORE, JointChannel.TransformSpace.LOCAL);
            this.matrixConfiguration = TransformChannelConfiguration.of((context) -> new Matrix4f().identity(), JointChannel.TransformType.IGNORE, JointChannel.TransformSpace.LOCAL);
            this.weightFunction = evaluationState -> 1f;
        }

        public Builder<P> setTranslation(Function<FunctionInterpolationContext, Vector3f> transformFunction, JointChannel.TransformType transformType, JointChannel.TransformSpace transformSpace){
            this.translationConfiguration = TransformChannelConfiguration.of(transformFunction, transformType, transformSpace);
            return this;
        }

        public Builder<P> setRotationQuaternion(Function<FunctionInterpolationContext, Quaternionf> transformFunction, JointChannel.TransformType transformType, JointChannel.TransformSpace transformSpace){
            this.rotationConfiguration = TransformChannelConfiguration.of(transformFunction, transformType, transformSpace);
            return this;
        }

        public Builder<P> setRotationEuler(Function<FunctionInterpolationContext, Vector3f> transformFunction, JointChannel.TransformType transformType, JointChannel.TransformSpace transformSpace){
            this.rotationConfiguration = TransformChannelConfiguration.of(context -> {
                Vector3f eulerRotation = transformFunction.apply(context);
                return new Quaternionf().rotationXYZ(eulerRotation.x(), eulerRotation.y(), eulerRotation.z());
            }, transformType, transformSpace);
            return this;
        }

        public Builder<P> setScale(Function<FunctionInterpolationContext, Vector3f> transformFunction, JointChannel.TransformType transformType, JointChannel.TransformSpace transformSpace){
            this.scaleConfiguration = TransformChannelConfiguration.of(transformFunction, transformType, transformSpace);
            return this;
        }

        public Builder<P> setMatrix(Function<FunctionInterpolationContext, Matrix4f> transformFunction, JointChannel.TransformType transformType, JointChannel.TransformSpace transformSpace){
            this.matrixConfiguration = TransformChannelConfiguration.of(transformFunction, transformType, transformSpace);
            return this;
        }

        public Builder<P> setWeight(Function<FunctionInterpolationContext, Float> weightFunction){
            this.weightFunction = weightFunction;
            return this;
        }

        public JointTransformerFunction<P> build(){
            return JointTransformerFunction.of(this);
        }
    }

    private record TransformChannelConfiguration<X>(Function<FunctionInterpolationContext, X> transformFunction, JointChannel.TransformType transformType, JointChannel.TransformSpace transformSpace){

        private static <X> TransformChannelConfiguration<X> of(Function<FunctionInterpolationContext, X> transformFunction, JointChannel.TransformType transformType, JointChannel.TransformSpace transformSpace){
            return new TransformChannelConfiguration<>(transformFunction, transformType, transformSpace);
        }
    }
}
