"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { Car } from "lucide-react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { useAuth } from "@/components/auth/auth-provider";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { ErrorMessage } from "@/components/ui/error-message";
import { Field, inputClassName } from "@/components/ui/form";
import { ApiError, readableApiError } from "@/lib/api/client";
import type { RegisterPayload } from "@/lib/api/auth";

const schema = z.object({
  email: z.string().email("Введите email"),
  password: z.string().min(8, "Минимум 8 символов"),
  confirmPassword: z.string().min(1, "Повторите пароль"),
  displayName: z.string().optional()
}).refine((value) => value.password === value.confirmPassword, {
  path: ["confirmPassword"],
  message: "Пароли не совпадают"
});

type FormValues = z.infer<typeof schema>;

export default function RegisterPage() {
  const router = useRouter();
  const { register: registerUser } = useAuth();
  const [apiError, setApiError] = useState<unknown>(null);
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      email: "",
      password: "",
      confirmPassword: "",
      displayName: ""
    }
  });

  async function onSubmit(values: FormValues) {
    setApiError(null);
    try {
      await registerUser(toRegisterPayload(values));
      router.replace("/vehicles");
    } catch (error) {
      setApiError(error);
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center px-4 py-10">
      <div className="w-full max-w-md">
        <div className="mb-8 text-center">
          <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-2xl border border-blue-400/30 bg-blue-500/15 text-neon-cyan shadow-glow">
            <Car className="h-7 w-7" />
          </div>
          <h1 className="text-3xl font-bold text-white">Auto<span className="text-neon-cyan">Blog</span></h1>
          <p className="mt-3 text-xl font-semibold text-white">Регистрация</p>
          <p className="mt-1 text-sm text-slate-400">Создайте аккаунт и начните историю автомобиля</p>
        </div>
        <Card>
          <form className="space-y-4" onSubmit={handleSubmit(onSubmit)}>
            <ErrorMessage
              message={authErrorMessage(apiError, "register")}
              details={apiError instanceof ApiError ? apiError.details : []}
              debug={apiError instanceof ApiError ? {
                status: apiError.status,
                backendMessage: apiError.message,
                path: apiError.path
              } : undefined}
            />
            <Field label="Email" error={errors.email?.message}>
              <input className={inputClassName()} autoComplete="email" {...register("email")} />
            </Field>
            <Field label="Пароль" error={errors.password?.message}>
              <input
                className={inputClassName()}
                type="password"
                autoComplete="new-password"
                placeholder="Введите пароль"
                {...register("password")}
              />
            </Field>
            <Field label="Повторите пароль" error={errors.confirmPassword?.message}>
              <input
                className={inputClassName()}
                type="password"
                autoComplete="new-password"
                placeholder="Повторите пароль"
                {...register("confirmPassword")}
              />
            </Field>
            <Field label="Имя" error={errors.displayName?.message}>
              <input className={inputClassName()} placeholder="Например, Алексей" {...register("displayName")} />
            </Field>
            <Button className="w-full" type="submit" disabled={isSubmitting}>
              {isSubmitting ? "Создаем…" : "Создать аккаунт"}
            </Button>
          </form>
          <p className="mt-6 text-center text-sm text-slate-400">
            Уже есть аккаунт?{" "}
            <Link href="/login" className="font-semibold text-neon-cyan hover:text-white">
              Войти
            </Link>
          </p>
        </Card>
      </div>
    </div>
  );
}

function toRegisterPayload(values: FormValues): RegisterPayload {
  return {
    email: values.email,
    password: values.password,
    displayName: values.displayName || undefined
  };
}

function authErrorMessage(error: unknown, mode: "login" | "register") {
  if (!error) {
    return null;
  }
  if (error instanceof ApiError) {
    if (error.status === undefined) {
      return "Backend недоступен. Проверьте, что API запущен на localhost:8080";
    }
    if (mode === "login" && error.status === 401) {
      return "Неверный email или пароль";
    }
    if (mode === "register" && error.status === 409) {
      return "Пользователь с таким email уже существует";
    }
    return error.message || readableApiError(error);
  }
  return readableApiError(error);
}
