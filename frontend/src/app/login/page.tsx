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

const schema = z.object({
  email: z.string().email("Введите адрес электронной почты"),
  password: z.string().min(1, "Введите пароль")
});

type FormValues = z.infer<typeof schema>;

export default function LoginPage() {
  const router = useRouter();
  const { login } = useAuth();
  const [apiError, setApiError] = useState<unknown>(null);
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      email: "",
      password: ""
    }
  });

  async function onSubmit(values: FormValues) {
    setApiError(null);
    try {
      await login(values);
      router.replace("/vehicles");
    } catch (error) {
      setApiError(error);
    }
  }

  return (
    <AuthScreen title="Рады видеть вас снова" subtitle="Заходите в свой гараж. История автомобиля уже ждёт вас.">
      <form className="space-y-5" onSubmit={handleSubmit(onSubmit)} noValidate>
        <ErrorMessage message={authErrorMessage(apiError, "login")} />
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
        <Field label="Пароль" error={errors.password?.message}>
          <input
            className={inputClassName("h-12")}
            type="password"
            autoComplete="current-password"
            placeholder="Введите пароль"
            aria-invalid={Boolean(errors.password)}
            {...register("password")}
          />
        </Field>
        <Button className="!mt-7 h-12 w-full" type="submit" disabled={isSubmitting}>
          {isSubmitting ? "Заходим в гараж…" : "Войти"}
          {!isSubmitting ? <ArrowRight className="h-4 w-4" /> : null}
        </Button>
      </form>
      <p className="mt-7 text-center text-sm text-muted">
        Ещё нет аккаунта?{" "}
        <Link href="/register" className="rounded-sm font-semibold text-ink underline decoration-brand-yellow decoration-2 underline-offset-4 hover:decoration-ink focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-4 focus-visible:outline-ink">
          Создать аккаунт
        </Link>
      </p>
    </AuthScreen>
  );
}
