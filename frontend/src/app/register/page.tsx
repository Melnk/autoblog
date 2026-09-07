"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { ArrowRight } from "lucide-react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { useAuth } from "@/components/auth/auth-provider";
import { AuthScreen, authErrorMessage } from "@/components/auth/auth-screen";
import { Button } from "@/components/ui/button";
import { ErrorMessage } from "@/components/ui/error-message";
import { Field, inputClassName } from "@/components/ui/form";
import type { RegisterPayload } from "@/lib/api/auth";

const schema = z.object({
  email: z.string().email("Введите адрес электронной почты"),
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
    <AuthScreen title="Познакомимся?" subtitle="Создайте аккаунт — и у вашего автомобиля появится свой уютный гараж.">
      <form className="space-y-4" onSubmit={handleSubmit(onSubmit)} noValidate>
        <ErrorMessage message={authErrorMessage(apiError, "register")} />
        <Field label="Как к вам обращаться?" error={errors.displayName?.message} hint="Необязательно">
          <input
            className={inputClassName("h-12")}
            autoComplete="name"
            placeholder="Например, Алексей"
            {...register("displayName")}
          />
        </Field>
        <Field label="Электронная почта" error={errors.email?.message}>
          <input
            className={inputClassName("h-12")}
            type="email"
            autoComplete="email"
            placeholder="you@example.ru"
            aria-invalid={Boolean(errors.email)}
            {...register("email")}
          />
        </Field>
        <Field label="Пароль" error={errors.password?.message} hint="Не менее 8 символов">
          <input
            className={inputClassName("h-12")}
            type="password"
            autoComplete="new-password"
            placeholder="Придумайте пароль"
            aria-invalid={Boolean(errors.password)}
            {...register("password")}
          />
        </Field>
        <Field label="Повторите пароль" error={errors.confirmPassword?.message}>
          <input
            className={inputClassName("h-12")}
            type="password"
            autoComplete="new-password"
            placeholder="Ещё раз, чтобы не ошибиться"
            aria-invalid={Boolean(errors.confirmPassword)}
            {...register("confirmPassword")}
          />
        </Field>
        <Button className="!mt-6 h-12 w-full" type="submit" disabled={isSubmitting}>
          {isSubmitting ? "Создаём аккаунт…" : "Создать аккаунт"}
          {!isSubmitting ? <ArrowRight className="h-4 w-4" /> : null}
        </Button>
      </form>
      <p className="mt-6 text-center text-sm text-muted">
        Уже с нами?{" "}
        <Link href="/login" className="rounded-sm font-semibold text-ink underline decoration-brand-yellow decoration-2 underline-offset-4 hover:decoration-ink focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-4 focus-visible:outline-ink">
          Войти
        </Link>
      </p>
    </AuthScreen>
  );
}

function toRegisterPayload(values: FormValues): RegisterPayload {
  return {
    email: values.email,
    password: values.password,
    displayName: values.displayName || undefined
  };
}
