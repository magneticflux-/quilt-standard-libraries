/*
 * Copyright 2022 QuiltMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.quiltmc.qsl.item.extension.mixin.crossbow;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import org.quiltmc.qsl.item.extension.api.crossbow.CrossbowExtensions;
import org.quiltmc.qsl.item.extension.api.crossbow.CrossbowShotProjectileEvents;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin implements CrossbowExtensions {
	@Inject(method = "createArrow", at = @At(value = "RETURN"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
	private static void createArrow(World world, LivingEntity entity, ItemStack crossbow, ItemStack projectileStack, CallbackInfoReturnable<PersistentProjectileEntity> cir, ArrowItem arrowItem, PersistentProjectileEntity persistentProjectileEntity) {
		persistentProjectileEntity = CrossbowShotProjectileEvents.CROSSBOW_REPLACE_SHOT_PROJECTILE.invoker().replaceProjectileShot(crossbow, projectileStack, entity, persistentProjectileEntity);
		CrossbowShotProjectileEvents.CROSSBOW_MODIFY_SHOT_PROJECTILE.invoker().modifyProjectileShot(crossbow, projectileStack, entity, persistentProjectileEntity);
		cir.setReturnValue(persistentProjectileEntity);
	}

	// Redirecting this method in order to get the item stack and shooting entity
	@Redirect(
			method = "use",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/item/CrossbowItem;shootAll(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/item/ItemStack;FF)V"
			)
	)
	private void shootAll(World world, LivingEntity entity, Hand hand, ItemStack stack, float speed, float divergence) {
		float newSpeed = stack.getItem() instanceof CrossbowExtensions crossbowItem ? crossbowItem.getProjectileSpeed(stack, entity) : speed;
		CrossbowItem.shootAll(world, entity, hand, stack, newSpeed, 1.f);
	}
}
